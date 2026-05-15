package com.seurteaching.digitaltwin.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.seurteaching.digitaltwin.machines.BaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.simulation.event.MachineStateEvent;
import com.seurteaching.digitaltwin.simulation.event.MachineStateListener;
import com.seurteaching.digitaltwin.simulation.util.EnergyStorage;
import com.seurteaching.digitaltwin.simulation.util.Packet;

/*
 * Base class for all simulated machines
 * T: Enum representing the state of the machine
 * M: The type of the underlying machine
 * thread safe
 */
public abstract class SimulationBaseMachine<T extends MachineState, M extends BaseMachine>
        implements Runnable, MachineStateListener {

    private static final Logger logger = LoggerFactory.getLogger(SimulationBaseMachine.class);

    // energy usage in Watt
    protected static final int IDLE_ENERGY_USAGE = 24;
    protected static final int ACTIVE_ENERGY_USAGE = 120;
    protected static final int RESTART_ENERGY_USAGE = 60;

    private static final int RESTART_TIME = 5000;
    private static final int OVERHEAT_TIMEOUT = 30000;

    private static final int HEAT_THRESHOLD = 20;

    private static final long MINIMIUM_FREEZE_MS = 1;
    private static final long FREEZE_WARN_ABS_MS = 10;

    private static final long TICK_WARN_ABS_MS = 4;

    private volatile MachineStateListener stateListener;
    protected final EnergyStorage energyStorage;

    private final String machineName;
    protected M baseMachine;

    private final Object stateLock = new Object();

    private int overheatingCounter = 0;
    private int heating = 0;

    // Of course its possible to have multiple packets in a machine, but for now we
    // only use one packet per machine
    private Packet packet;
    private T state;

    private int processedSteps = 0;

    protected volatile boolean running = false;

    private long freezeTimeMs = 0;

    private final double speedMultiplier;
    private final double machineSpeedMultiplier;

    @Autowired
    private SimulationController simulationController;

    public SimulationBaseMachine(String machineName, M baseMachine, double speedMultiplier, double machineSpeedMultiplier) {
        this.machineName = machineName;
        this.baseMachine = baseMachine;
        this.speedMultiplier = speedMultiplier;
        this.machineSpeedMultiplier = machineSpeedMultiplier;
        this.energyStorage = new EnergyStorage(speedMultiplier);

        // set listener to be able to send events to the controlling unit
        this.stateListener = simulationController;
    }

    protected abstract T getDefaultState();

    @Override
    public void run() {
        while (running) {
            try {
                freezeTimeMs = 0;

                long startMs = System.currentTimeMillis();
                checkOverheating();
                
                tick(getState());

                long endMs = System.currentTimeMillis();
                long durationMs = endMs - startMs;
                long durationMsExclFreeze = durationMs - freezeTimeMs;
                
                if(durationMsExclFreeze >= TICK_WARN_ABS_MS) {
                    logger.warn("[LAG] " + machineName + " tick took " + durationMsExclFreeze + "ms");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.info("Machine " + machineName + " stopped");
    }

    protected abstract void tick(T currentState) throws InterruptedException;

    /*
     * start / stop methods
     */

    public void stopMachine() {
        running = false;
        this.energyStorage.stopRecording();
    }

    public void startMachine() {
        running = true;
        this.energyStorage.reset();
        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

        synchronized(stateLock) {
            packet = null;
            state = getDefaultState();
            heating = 0;
            overheatingCounter = 0;
            processedSteps = 0;
        }
    }

    // is called from the SimulationController tick thread only
    // handle state change of other machines here
    public void onStateChange(MachineStateEvent event) {}

    /**
     * Freezes the machine for a specified duration, adjusted by the machine's
     * delay.
     * 
     * @param milliseconds The duration to freeze in milliseconds.
     * @throws InterruptedException If the thread is interrupted while sleeping.
     */
    protected void freeze(long milliseconds) throws InterruptedException {
        double factor = machineSpeedMultiplier * speedMultiplier;
        long expectedMs = Math.max(MINIMIUM_FREEZE_MS, Math.round(milliseconds / factor));
    
        long startNs = System.nanoTime();
        Thread.sleep(expectedMs);
        long actualMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        freezeTimeMs += actualMs;
    
        long overshootMs = actualMs - expectedMs; // can be negative if the thread woke early (rare)
        boolean tooLate = overshootMs >= FREEZE_WARN_ABS_MS;

        if (tooLate) {
            logger.warn("[LAG] " + machineName + " freeze expected=" + expectedMs + "ms actual=" + actualMs + "ms (+" + overshootMs + "ms)");
        }
    }

    /*
     * state methods
     */

    public T getState() {
        synchronized(stateLock) {
            return state; //state is immutable enum, so this is thread safe
        }
    }

    protected void setState(T newState) {
        boolean changed = false;
        synchronized(stateLock) {
            if (this.state != newState) {
                this.state = newState;
                changed = true;
            }
        }

        if (changed && stateListener != null) {
            MachineStateEvent event = new MachineStateEvent(this, newState);
            stateListener.onStateChange(event);
        }
    }

    /*
     * packet methods
     */

    public final boolean tryTransferPacketTo(
        SimulationBaseMachine<?, ?> target,
        MachineState sourceState,
        MachineState targetState,
        int targetHeatIncrement) {

        // Total order to avoid deadlocks
        SimulationBaseMachine<?, ?> first = this;
        SimulationBaseMachine<?, ?> second = target;

        if(first == second) {
            return false;
        }
        
        if (System.identityHashCode(first) > System.identityHashCode(second)) {
            first = target;
            second = this;
        }

        if(System.identityHashCode(first) == System.identityHashCode(second)) {
            logger.error("Hash collision in tryTransferPacketTo");
            return false;
        }

        boolean success = false;

        synchronized (first.stateLock) {
        synchronized (second.stateLock) {
            
            // only transfer if packet is available and target has no packet
            if (this.packet == null || target.packet != null) {
                return false;
            }
            // check state
            if (this.state != sourceState || target.state != targetState) {
                return false;
            }
            Packet p = this.packet;
            this.packet = null;

            p.getPacketLocation().add(target.machineName);
            p.incrementIndex();

            target.packet = p;
            target.heating += targetHeatIncrement;
            target.processedSteps++;
            success = true;

            logger.debug("Packet " + p.getId() + " transferred from " + this.machineName + " to " + target.machineName);
        }
        }

        if(success) {
            target.postReceivePacket();
            this.postEjectPacket();
        }
        return success;
    }

    protected void postReceivePacket() {}
    protected void postEjectPacket() {}

    public boolean tryReceivePacket(Packet packet) {
        boolean success = false;
        synchronized(stateLock) {
            if(this.packet != null) {
                return false;
            }
            this.packet = packet;
            this.processedSteps++;
            success = true;
        }

        if(success) {
            postReceivePacket();
        }
        return success;
    }

    protected Packet getPacketSnapshot() {
        synchronized(stateLock) {
            if(this.packet == null) {
                return null;
            }
            return (Packet) this.packet.clone();
        }
    }

    protected Packet clearAndGetPacket() {
        synchronized(stateLock) {
            Packet p = this.packet;
            this.packet = null;
            return p;
        }
    }

    /*
     * heating methods
     */

    public void decreaseHeating(int decreaseBy) {
        synchronized(stateLock) {
            heating -= decreaseBy;
            if(heating < 0) {
                heating = 0;
            }
        }
    }

    protected void increaseHeating(int increaseBy) {
        synchronized(stateLock) {
            heating += increaseBy;
        }
    }

    public int getHeating() {
        synchronized(stateLock) {
            return heating;
        }
    }

    public int getOverheatingCounter() {
        synchronized(stateLock) {
            return overheatingCounter;
        }
    }

    protected void checkOverheating() {
        boolean triggered = false;
        synchronized(stateLock) {
            if(heating > HEAT_THRESHOLD) {
                overheatingCounter++;
                heating = 0;
                triggered = true;
            }
        }

        if(!triggered) {
            return;
        }

        logger.info(
                "OVERHEATING in " + machineName + "! Shutting down for " + (OVERHEAT_TIMEOUT / 1000) + " seconds.");
        this.energyStorage.setEnergyUsage(0);
        this.baseMachine.setState(com.seurteaching.digitaltwin.machines.MachineState.OVERHEATED);
        try {
            freeze(OVERHEAT_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.energyStorage.setEnergyUsage(RESTART_ENERGY_USAGE);

        try {
            freeze(RESTART_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.baseMachine.setState(com.seurteaching.digitaltwin.machines.MachineState.IDLE);
        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

    }

    /*
     * getter
     */

    public String getMachineName() {
        return machineName;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public M getBaseMachine() {
        return baseMachine;
    }

    public int getProcessedSteps() {
        synchronized(stateLock) {
            return processedSteps;
        }
    }

    public int getHeatThreshold() {
        return HEAT_THRESHOLD;
    }
}