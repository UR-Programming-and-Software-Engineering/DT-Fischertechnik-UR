package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.simulation.event.MachineStateEvent;
import com.seurteaching.digitaltwin.simulation.util.Packet;
import com.seurteaching.digitaltwin.machines.Highbay;

import java.util.ArrayList;

import static com.seurteaching.digitaltwin.machines.MachineState.*;

public class HighBay01 extends SimulationBaseMachine<HighBay01.HighBay01State, Highbay> {

    private VacuumGripper01 vacuumGripper01;
    private MultiProcessing01 multiProcessing01;

    private boolean gripperWasInStateIdle3 = false;

    private ArrayList<Packet> storedPackets;

    public static enum HighBay01State implements MachineState {
        NotReadyToReceive,
        GettingReadyToReceive, 
        WaitingForGripperToLoad,
        WaitingForGripperIdle3,
        StoringPacket,
        ;

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }
    
    public HighBay01() {
        this(1.0, 1.0);
    }

    public HighBay01(double machineSpeedMultiplier) {
        this(1.0, machineSpeedMultiplier);
    }

    public HighBay01(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "HighBay01",
            new Highbay(
                "HighBay01",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                false,
                true,
                false,
                false,
                100,
                200,
                true,
                false
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
        this.storedPackets = new ArrayList<>();
    }

    public void setVacuumGripper01(VacuumGripper01 vacuumGripper01) {
        this.vacuumGripper01 = vacuumGripper01;
    }
    public void setMultiProcessing01(MultiProcessing01 multiProcessing01) {
        this.multiProcessing01 = multiProcessing01;
    }

    @Override
    public void tick(HighBay01State state) throws InterruptedException {
        switch (state) {
            case NotReadyToReceive:
                this.baseMachine.setState(IDLE);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setCantileverForward(false);
                this.baseMachine.setCantileverBackward(false);
                this.baseMachine.setCantileverUp(false);
                this.baseMachine.setCantileverDown(false);
                this.baseMachine.setConveyorForward(false);
                this.baseMachine.setConveyorBackward(false);
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                freeze(100);

                //the gripper would like to give you the packet from the multi processing machine
                if(vacuumGripper01.getState() == VacuumGripper01.VacuumGripper01State.Idle3 &&
                    multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingOutgoing) {
                    setState(HighBay01State.GettingReadyToReceive);
                }

                break;
            case GettingReadyToReceive:
                this.baseMachine.setState(RUNNING);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setCantileverForward(true);
                this.baseMachine.setCantileverBackward(true);
                this.baseMachine.setCantileverUp(true);
                this.baseMachine.setCantileverDown(true);
                this.baseMachine.setConveyorForward(true);
                this.baseMachine.setConveyorBackward(false);
                this.baseMachine.setHorizontal(50);
                this.baseMachine.setHeight(400);
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                freeze(32000);

                setState(HighBay01State.WaitingForGripperToLoad);
                break;
            case WaitingForGripperToLoad:
                this.baseMachine.setState(IDLE);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setCantileverForward(false);
                this.baseMachine.setCantileverBackward(false);
                this.baseMachine.setCantileverUp(false);
                this.baseMachine.setCantileverDown(false);
                this.baseMachine.setConveyorForward(false);
                this.baseMachine.setConveyorBackward(false);
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                freeze(100);

                break;
            case WaitingForGripperIdle3:
                if(gripperWasInStateIdle3) {
                    setState(HighBay01State.StoringPacket);
                    gripperWasInStateIdle3 = false;
                }
            case StoringPacket:
                this.baseMachine.setState(RUNNING);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setCantileverForward(true);
                this.baseMachine.setCantileverBackward(true);
                this.baseMachine.setCantileverUp(true);
                this.baseMachine.setCantileverDown(true);
                this.baseMachine.setConveyorForward(false);
                this.baseMachine.setConveyorBackward(true);
                this.baseMachine.setHorizontal(100);
                this.baseMachine.setHeight(200);
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                freeze(32000);

                this.storePacket();
                setState(HighBay01State.NotReadyToReceive);
                break;
        }       
    }

    @Override
    public HighBay01State getDefaultState() {
        return HighBay01State.NotReadyToReceive;
    }

    private void storePacket() {
        Packet packet = clearAndGetPacket();
        if(packet != null) {
            // System.out.println("[HighBay] stored packet " + packet.getId());
            packet.setDelivered();
            packet.incrementIndex();
            this.storedPackets.add(packet);
        }else {
            System.err.println("[HighBay] missing packet?");
        }
    }

    @Override
    protected void postReceivePacket() {
        setState(HighBay01State.WaitingForGripperIdle3);
    }

    @Override
    public void onStateChange(MachineStateEvent event) {
        if(event.getMachine() instanceof VacuumGripper01) {
            //ensure that we dont miss the Idle3 state of the gripper
            if(getState() != HighBay01State.WaitingForGripperIdle3) {
                return;
            }
            if (event.getNewState() == VacuumGripper01.VacuumGripper01State.Idle3) {
                gripperWasInStateIdle3 = true;
            }
        }
    }


}
