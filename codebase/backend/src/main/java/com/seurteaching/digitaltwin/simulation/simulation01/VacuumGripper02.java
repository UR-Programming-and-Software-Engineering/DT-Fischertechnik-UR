package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.simulation.simulation01.ConveyorBelt01.ConveyorBelt01State;
import com.seurteaching.digitaltwin.machines.VacuumGripper;
import static com.seurteaching.digitaltwin.machines.MachineState.*;

public class VacuumGripper02 extends SimulationBaseMachine<VacuumGripper02.VacuumGripper02State, VacuumGripper> {

    private SortingLine01 sortingLine01;
    private ConveyorBelt01 conveyorBelt01;

    public static enum VacuumGripper02State implements MachineState {
        P1, //Idle position
        P1toP2, //move from idle to soring line
        P2, //await packet from sorting line 
        P2toP3, //move the packet from sorting line to conveyor belt
        P3, //place packet on conveyor belt
        P3toP4, // move up to hover over coneyor belt (probably because the original machine is not well designed)
        P4, //hover position over conveyor belt (wait for sorting line to be ready)
        P4toP2; // move from the hover position to the sorting line to take next packet

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }

    public VacuumGripper02() {
        this(1.0, 1.0);
    }

    public VacuumGripper02(double machineSpeedMultiplier) {
        this(1.0, machineSpeedMultiplier);
    }

    public VacuumGripper02(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "VacuumGripper02",
            new VacuumGripper(
                "VacuumGripper02",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                333,
                222,
                111
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
    }

    public void setSortingLine01(SortingLine01 sortingLine01) {
        this.sortingLine01 = sortingLine01;
    }
    public void setConveyorBelt01(ConveyorBelt01 conveyorBelt01) {
        this.conveyorBelt01 = conveyorBelt01;
    }

    @Override
    public void tick(VacuumGripper02State state) throws InterruptedException {
                switch (state) {
                    case P1:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        //both machines are ready to exchange packets
                        //another option would be to get the packet from sorting line and then wait for the conveyor belt to be ready
                        //this would be harder to implement
                        if(conveyorBelt01.getState() == ConveyorBelt01State.WaitingIncoming && sortingLine01.isInStateWaitingOutgoing()) {
                            setState(VacuumGripper02State.P1toP2);
                        }

                        break;
                    case P1toP2:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(400);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(12000);

                        setState(VacuumGripper02State.P2);
                        break;
                    case P2:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case P2toP3:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(450);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(16000);

                        setState(VacuumGripper02State.P3);
                        break;
                    case P3:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case P3toP4:
                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);
                        // any reason why the base machine does not change?
                        freeze(2000);
                        this.baseMachine.deltaRotation(500);
                        setState(VacuumGripper02State.P4);
                        break;
                    case P4:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        if(conveyorBelt01.getState() == ConveyorBelt01State.WaitingIncoming && sortingLine01.isInStateWaitingOutgoing()) {
                            setState(VacuumGripper02State.P4toP2);
                        }

                        break;
                    case P4toP2:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.deltaRotation(400);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(3000);
                        
                        setState(VacuumGripper02State.P2);
                        break;
                }
    }

    @Override
    public VacuumGripper02State getDefaultState() {
        return VacuumGripper02State.P1;
    }

    @Override
    protected void postEjectPacket() {
        setState(VacuumGripper02State.P3toP4);
    }

    @Override
    protected void postReceivePacket() {
        setState(VacuumGripper02State.P2toP3);
    }


}