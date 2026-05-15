package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.machines.VacuumGripper;
import static com.seurteaching.digitaltwin.machines.MachineState.*;
public class VacuumGripper01 extends SimulationBaseMachine<VacuumGripper01.VacuumGripper01State, VacuumGripper> {

    private ConveyorBelt01 conveyorBelt01;
    private HighBay01 highBay01;
    private MultiProcessing01 multiProcessing01;

    public static enum VacuumGripper01State implements MachineState {
        Idle, //initial state, waiting for the conveyor belt
        IdleToConveyor, 
        Conveyor, 
        ConveyorToMultiIncoming, 
        MultiIncoming, //move to multi processing line to drop packet
        MultiIncomingToIdle2, 
        Idle2, //hover position above multi processing line
        Idle2ToIdle3, //multiprocessing line is waiting to give away the packet
        Idle3, //hover position, so the highbay can adjust
        Idle3ToMultiOutgoing,
        MultiOutgoing,
        MultiOutgoingToHighbay,
        Highbay,
        HighbayToIdle3,

        //states to handle concurrent incoming packets
        Idle3ToConveyor, //when the packet is delivered to the highbay a new one can be picked up
        ;

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }

    public VacuumGripper01() {
        this(1.0, 1.0);
    }

    public VacuumGripper01(double machineSpeedMultiplier) {
        this(1.0, machineSpeedMultiplier);
    }

    public VacuumGripper01(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "VacuumGripper01",
            new VacuumGripper(
                "VacuumGripper01",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                200,
                100,
                50
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
    }

    @Override
    public void tick(VacuumGripper01State state) throws InterruptedException {
                switch (state) {
                    case Idle:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        //the packet has to move from the conveyor belt to the multi processing line
                        //instead of waiting for the conveyor belt to change state to WaitingOutgoing we are smart and already move to grab the packet
                        if(
                            conveyorBelt01.getState() == ConveyorBelt01.ConveyorBelt01State.Running && 
                            multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingIncoming
                        ){
                            setState(VacuumGripper01State.IdleToConveyor);
                        }

                        break;
                    case IdleToConveyor:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(7000);

                        setState(VacuumGripper01State.Conveyor);
                        break;
                    case Conveyor:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaForward(100);
                        this.baseMachine.deltaHeight(50);
                        this.baseMachine.deltaForward(50);
                        this.baseMachine.deltaHeight(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case ConveyorToMultiIncoming:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(300);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(5000);

                        setState(VacuumGripper01State.MultiIncoming);
                        break;
                    case MultiIncoming:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaForward(100);
                        this.baseMachine.deltaHeight(50);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case MultiIncomingToIdle2:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaForward(50);
                        this.baseMachine.deltaHeight(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(5000);

                        setState(VacuumGripper01State.Idle2);
                        break;
                    case Idle2:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        if(multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingOutgoing) {
                            setState(VacuumGripper01State.Idle2ToIdle3);
                        }

                        break;
                    case Idle2ToIdle3:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(350);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(3000);

                        setState(VacuumGripper01State.Idle3);
                        break;
                    case Idle3:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        //smart gripper logic
                        if(
                            conveyorBelt01.getState() == ConveyorBelt01.ConveyorBelt01State.Running && 
                            multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingIncoming
                        ){
                            setState(VacuumGripper01State.IdleToConveyor);
                        }

                        //conveyor belt and multi processing line are ready to exchange packets
                        if(
                            conveyorBelt01.getState() == ConveyorBelt01.ConveyorBelt01State.WaitingOutgoing && 
                            multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingIncoming
                        ) {
                            setState(VacuumGripper01State.Idle3ToConveyor);
                        }

                        //highbay and multi processing line are ready to exchange packets
                        if(
                            multiProcessing01.getState() == MultiProcessing01.MultiProcessing01State.WaitingOutgoing && 
                            highBay01.getState() == HighBay01.HighBay01State.WaitingForGripperToLoad)
                        {
                            setState(VacuumGripper01State.Idle3ToMultiOutgoing);
                        }

                        break;
                    case Idle3ToMultiOutgoing:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(300);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(15000);

                        setState(VacuumGripper01State.MultiOutgoing);
                        break;
                    case MultiOutgoing:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaForward(100);
                        this.baseMachine.deltaHeight(50);
                        this.baseMachine.deltaForward(50);
                        this.baseMachine.deltaHeight(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case MultiOutgoingToHighbay:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(400);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(6000);

                        setState(VacuumGripper01State.Highbay);
                        break;
                    case Highbay:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaForward(100);
                        this.baseMachine.deltaHeight(50);
                        this.baseMachine.deltaForward(50);
                        this.baseMachine.deltaHeight(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case HighbayToIdle3:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(350);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(3000);

                        setState(VacuumGripper01State.Idle3);
                        break;
                    case Idle3ToConveyor:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(100);
                        this.baseMachine.deltaForward(100);
                        this.baseMachine.deltaHeight(50);
                        this.baseMachine.deltaForward(50);
                        this.baseMachine.deltaHeight(100);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(2000);

                        setState(VacuumGripper01State.Conveyor);
                        break;
                }
    }

    @Override
    public VacuumGripper01State getDefaultState() {
        return VacuumGripper01State.Idle;
    }

    @Override
    protected void postEjectPacket() {
        if(getState() == VacuumGripper01State.Highbay) {
            setState(VacuumGripper01State.HighbayToIdle3);
        }else if(getState() == VacuumGripper01State.MultiIncoming) {
            setState(VacuumGripper01State.MultiIncomingToIdle2);
        }
    }

    @Override
    protected void postReceivePacket() {
        if(getState() == VacuumGripper01State.Conveyor) {
            setState(VacuumGripper01State.ConveyorToMultiIncoming);
        }else if(getState() == VacuumGripper01State.Highbay) {
            setState(VacuumGripper01State.HighbayToIdle3);
        }else if(getState() == VacuumGripper01State.MultiOutgoing) {
            setState(VacuumGripper01State.MultiOutgoingToHighbay);
        }
    }

    public void setConveyorBelt01(ConveyorBelt01 conveyorBelt01) {
        this.conveyorBelt01 = conveyorBelt01;
    }
    public void setHighBay01(HighBay01 highBay01) {
        this.highBay01 = highBay01;
    }
    public void setMultiProcessing01(MultiProcessing01 multiProcessing01) {
        this.multiProcessing01 = multiProcessing01;
    }
    
}
