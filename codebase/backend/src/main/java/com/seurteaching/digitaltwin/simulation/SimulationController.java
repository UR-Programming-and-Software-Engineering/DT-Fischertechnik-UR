package com.seurteaching.digitaltwin.simulation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.seurteaching.digitaltwin.machines.Color;
import com.seurteaching.digitaltwin.model.SimulationDataDetails;
import com.seurteaching.digitaltwin.model.SimulationMachineData;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.simulation.event.MachineStateEvent;
import com.seurteaching.digitaltwin.simulation.event.MachineStateListener;
import com.seurteaching.digitaltwin.simulation.util.EnergyStorage;
import com.seurteaching.digitaltwin.simulation.util.Packet;
import com.seurteaching.digitaltwin.simulation.util.PacketManager;
import com.seurteaching.digitaltwin.simulation.util.PacketPass;
import com.seurteaching.digitaltwin.simulation.util.SimulationConfig;
import com.seurteaching.digitaltwin.simulation.util.SimulationConfigFactory;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Component
public class SimulationController implements MachineStateListener {

    private static Logger logger = LoggerFactory.getLogger(SimulationController.class);

    private static final long PROCESS_INTERVAL_MS = 5;
    private static final long COOLING_INTERVAL_MS = 180_000; // 3 minutes
    private static final int DECREASE_HEATING_VALUE = 1;
    private static final int INCREASE_HEATING_VALUE = 1;

    @Autowired private EntityManager em;

    private static final Random RANDOM = new Random();

    private volatile SimulationDataDetails simulationDataDetails;
    private SimulationConfig config = SimulationConfigFactory.createDefaultConfig();

    private long simulationEndMS;
    private Instant simulationStartInstant;
    private Instant simulationEndInstant;

    private String[] packePath;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isWorking = new AtomicBoolean(false);

    private long lastCoolingTimeMs;
    private long lastTickInvocationMs = 0;

    private ExecutorService executorService;
    private List<Future<?>> machineFutures;

    private PacketManager packetManager;

    private ConcurrentLinkedQueue<MachineStateEvent> eventQueue;

    @Transactional
    public void startSimulation(SimulationConfig config) {

        if (!isWorking.compareAndSet(false, true)) {
            throw new IllegalStateException("Simulation start/stop already in progress");
        }

        try {

            if (!isRunning.compareAndSet(false, true)) throw new IllegalStateException("Simulation is already running");

            this.config = config;

            eventQueue = new ConcurrentLinkedQueue<>();     
            packetManager = new PacketManager(config.getSpeedMultiplier());

            executorService = Executors.newFixedThreadPool(config.getMachines().size());
            machineFutures = new ArrayList<>();

            for (SimulationBaseMachine<?, ?> machine : config.getMachines()) {
                machine.startMachine();
                Future<?> future = executorService.submit(machine::run);
                machineFutures.add(future);
            }

            this.lastCoolingTimeMs = System.currentTimeMillis();
            this.lastTickInvocationMs = System.currentTimeMillis();

            this.simulationStartInstant = Instant.now();
            this.simulationEndMS = simulationStartInstant.toEpochMilli() + config.getSimulationDuration();
            this.simulationEndInstant = Instant.ofEpochMilli(simulationEndMS);

            logger.info("--- Simulation started ---");
            logger.info("config: " + config);
            logger.info("started at " + toLocalDateTime(this.simulationStartInstant.toEpochMilli()));
            logger.info("stopping at " + toLocalDateTime(this.simulationEndMS));
            logger.info("---");

            this.simulationDataDetails = buildSimulationDataDetails();
            // Persist immediately to get the ID
            saveSimulationDataDetails();

        } finally {
            isWorking.set(false);
        }

    }

    @Transactional
    public void stopSimulation() {
        if (!isWorking.compareAndSet(false, true)) {
            throw new IllegalStateException("Simulation start/stop already in progress");
        }

        try {

            if (!isRunning.compareAndSet(true, false)) throw new IllegalStateException("Simulation is not running");

            this.simulationDataDetails = updateSimulationData(this.simulationDataDetails);
            this.simulationDataDetails.setTimeEnded(toLocalDateTime(System.currentTimeMillis()));
            saveSimulationDataDetails();

            for (SimulationBaseMachine<?,?> machine : config.getMachines()) {
                machine.stopMachine();
            }

            for (Future<?> future : machineFutures) {
                future.cancel(true);
            }
            if (executorService != null) {
                executorService.shutdownNow();
            }
            machineFutures.clear();

        } finally {
            isWorking.set(false);
        }

    }

    private void handlePacketGeneration() {
        final List<SimulationBaseMachine<?, ?>> machinesSnap = config.getMachines();
        if (machinesSnap.isEmpty()) return;

        Packet lastPacket = packetManager.getLastPacketSnapshot();
        Color[] colors = Color.values();
        Color randomColor = colors[RANDOM.nextInt(colors.length)];
        long now = System.currentTimeMillis();

        long simulationNow = Math.round((now - this.simulationStartInstant.toEpochMilli()) * config.getSpeedMultiplier());

        if (lastPacket == null) {
            Packet p = new Packet(0, now, simulationNow, randomColor);
            p.getPacketLocation().add(machinesSnap.get(0).getMachineName());
            if (machinesSnap.get(0).tryReceivePacket(p)) {
                logger.info("New packet created: " + p.getId());
                packetManager.addPacket(p);
            }
            return;
        }

        long dt = now - lastPacket.getCreationTimestamp();
        if (dt > config.getPacketCreationIntervalMs()) {
            Packet p = new Packet(lastPacket.getId() + 1, now, simulationNow, randomColor);
            p.getPacketLocation().add(machinesSnap.get(0).getMachineName());
            if (machinesSnap.get(0).tryReceivePacket(p)) {
                logger.info("New packet created: " + p.getId());
                packetManager.addPacket(p);
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = PROCESS_INTERVAL_MS)
    public void tick() {
        if(isRunning.get() == false) return;
        if(isWorking.get() == true) return;

        long startMS = System.currentTimeMillis();

        if (lastTickInvocationMs > 0) {
            long gap = startMS - lastTickInvocationMs;
            if (gap > PROCESS_INTERVAL_MS + 10) {
                logger.warn("[LAG] tick gap was " + gap + "ms (should be " + PROCESS_INTERVAL_MS + "ms)");
            }
        }
        lastTickInvocationMs = startMS;

        if (System.currentTimeMillis() >= this.simulationEndInstant.toEpochMilli()) {
            stopSimulation();
            return;
        }

        final var machinesSnap = config.getMachines(); 

        long now = System.currentTimeMillis();
        double coolingIntervalRealMs = COOLING_INTERVAL_MS / config.getSpeedMultiplier();
        if (now - lastCoolingTimeMs >= coolingIntervalRealMs) {
            for (SimulationBaseMachine<?, ?> m : machinesSnap) {
                m.decreaseHeating(DECREASE_HEATING_VALUE);
            }
            lastCoolingTimeMs = now;
        }

        handlePacketGeneration();

        sendEventsToMachines();

        for (PacketPass pass : config.getEdges()) {

            SimulationBaseMachine<?,?> source = pass.source;
            SimulationBaseMachine<?,?> target = pass.target;
            MachineState sourceState = pass.sourceState;
            MachineState targetState = pass.targetState;

            if(source.getState() == sourceState && target.getState() == targetState) {
                //attempt transfer if correct states and packet is available
                source.tryTransferPacketTo(target, sourceState, targetState, INCREASE_HEATING_VALUE);
            }
        }

        long endMS = System.currentTimeMillis();
        long durationMS = endMS - startMS;
        if (durationMS > PROCESS_INTERVAL_MS) {
            logger.warn("[LAG] tick took longer (" + durationMS + "ms) than " + PROCESS_INTERVAL_MS + "ms");
        }
    }

    @Override
    public void onStateChange(MachineStateEvent event) {
        logger.debug("[SimulationController] received state change event: " + event.getMachine().getMachineName() + " changed to " + event.getNewState());

        eventQueue.add(event);
    }

    private void sendEventsToMachines() {
        MachineStateEvent event;
        while ((event = eventQueue.poll()) != null) {
            final SimulationBaseMachine<?,?> origin = event.getMachine();
            final List<SimulationBaseMachine<?,?>> machinesSnap = config.getMachines();
            for (var m : machinesSnap) {
                if (m != origin) {
                    try {
                        m.onStateChange(event);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("Shutting down simulation controller...");
        stopSimulation();
    }

    // Getters (return snapshots / unmodifiable views)

    public String[] getPackePath() {
        return packePath == null ? null : packePath.clone();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    /*
     * Fetch the current simulation
     * @return the current SimulationDataDetails (with updated data)
     * side effect: updates the internal simulationDataDetails instance
     */
    public SimulationDataDetails getRunningSimulation(){
        if(this.isRunning.get() == true) {
            // update data before returning (side effect)
            this.simulationDataDetails = updateSimulationData(this.simulationDataDetails);
        }
        return this.simulationDataDetails;
    }

    public ArrayList<Packet> getPackets() {
        return new ArrayList<>(packetManager.getPacketsSnapshot());
    }

    public List<SimulationBaseMachine<?, ?>> getMachines() {
        return config.getMachines(); // already unmodifiable due to List.copyOf
    }

    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    // update simulation data details

    private List<Double> getKWhPerHour() {
        List<Double> aggregated = new ArrayList<>();
        final var machines = config.getMachines();
        for (SimulationBaseMachine<?, ?> machine : machines) {
            EnergyStorage storage = machine.getEnergyStorage();
            if (storage != null) {
                List<Double> kwh = storage.getWhPerHour();
                for (int i = 0; i < kwh.size(); i++) {
                    if (aggregated.size() <= i) aggregated.add(kwh.get(i));
                    else aggregated.set(i, aggregated.get(i) + kwh.get(i));
                }
            }
        }
        return aggregated;
    }

    private List<Double> getKWhPerMinute() {
        List<Double> aggregated = new ArrayList<>();
        final var machines = config.getMachines();
        for (SimulationBaseMachine<?, ?> machine : machines) {
            EnergyStorage storage = machine.getEnergyStorage();
            if (storage != null) {
                List<Double> kwh = storage.getWhPerMinute();
                for (int i = 0; i < kwh.size(); i++) {
                    if (aggregated.size() <= i) aggregated.add(kwh.get(i));
                    else aggregated.set(i, aggregated.get(i) + kwh.get(i));
                }
            }
        }
        return aggregated;
    }

    private double getTotalKWh() {
        double total = 0.0;
        final var machines = config.getMachines();
        for (SimulationBaseMachine<?, ?> machine : machines) {
            EnergyStorage storage = machine.getEnergyStorage();
            if (storage != null) total += storage.getTotalKWH();
        }
        return total;
    }

    private List<SimulationMachineData> updateSimulationMachineData(List<SimulationMachineData> existingData) {
        final var machines = config.getMachines();
        for (SimulationBaseMachine<?, ?> machine : machines) {
            
            SimulationMachineData md = existingData.stream()
                .filter(d -> d.getName().equals(machine.getMachineName()))
                .findFirst()
                .orElse(null);
            if (md == null) continue; // should not happen
            md.setName(machine.getMachineName());
            EnergyStorage storage = machine.getEnergyStorage();
            md.setEnergy(storage != null ? storage.getTotalKWH() : 0.0);
            md.setOverheatingCounter(machine.getOverheatingCounter());
            md.setProcessedSteps(machine.getProcessedSteps());
        }
        return existingData;
    }

    private SimulationDataDetails buildSimulationDataDetails() {
        SimulationDataDetails simulationData = new SimulationDataDetails();

        simulationData.setName("Simulation_" + formatTimestampIso(this.simulationStartInstant.toEpochMilli()));
        simulationData.setTimeCreated(toLocalDateTime(this.simulationStartInstant.toEpochMilli()));
        simulationData.setDuration(String.valueOf(this.config.getSimulationDuration()));
        simulationData.setExpectedPackages(this.config.getExpectedPackages());
        simulationData.setPackageThroughputEstimated(this.config.getPackageThroughputEstimated());
        simulationData.setTimePerPackageEstimated(this.config.getTimePerPackageEstimated());
        simulationData.setSimulationParameters(config.getSimulationParameters());

        simulationData.setTotalEnergyPerHour(new ArrayList<>());
        simulationData.setTotalEnergyPerMinute(new ArrayList<>());

        simulationData.setMachineData(buildSimulationMachineData());
        for (SimulationMachineData md : simulationData.getMachineData()) {
            md.setSimulationDataDetails(simulationData);
        }
        return simulationData;
    }

    private List<SimulationMachineData> buildSimulationMachineData() {
        List<SimulationMachineData> machineDataList = new ArrayList<>();
        final var machines = config.getMachines();
        for (SimulationBaseMachine<?, ?> machine : machines) {
            SimulationMachineData md = new SimulationMachineData();
            md.setName(machine.getMachineName());
            md.setProcessedSteps(0);
            md.setEnergy(0.0);
            md.setOverheatingCounter(0);
            machineDataList.add(md);
        }
        return machineDataList;
    }

    private void saveSimulationDataDetails() {
        if (this.simulationDataDetails.getId() == null) em.persist(this.simulationDataDetails);
        else em.merge(this.simulationDataDetails);
        em.flush();
    }

    private SimulationDataDetails updateSimulationData(SimulationDataDetails simulationData) {
        if (simulationData == null) return simulationData;

        simulationData.setTotalPackages(packetManager.getPacketCount());

        double simulatedMinutes = this.config.getSimulatedTimeInMinutes();
        double simulatedHours = simulatedMinutes / 60.0;
        if (simulatedHours > 0) {
            simulationData.setPackageThroughputReal(packetManager.getPacketCount() / simulatedHours);
            simulationData.setTimePerPackageReal(simulatedHours / Math.max(packetManager.getPacketCount(), 1) * 60); // in minutes
        }

        simulationData.setTotalEnergyPerHour(getKWhPerHour());
        simulationData.setTotalEnergyPerMinute(getKWhPerMinute());
        simulationData.setTotalEnergyUsage(getTotalKWh());

        simulationData.setHourlyPackages(packetManager.getHourlyPackagesSnapshot());

        List<SimulationMachineData> newMachineData = updateSimulationMachineData(simulationData.getMachineData());
        simulationData.setMachineData(newMachineData);

        return simulationData;
    }

    /*
     * Utility methods
     */

    private LocalDateTime toLocalDateTime(long timestampMS) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMS), ZoneId.of("Europe/Berlin"));
    }

    private String formatTimestampIso(long timestampMS) {
        LocalDateTime dt = toLocalDateTime(timestampMS);
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
