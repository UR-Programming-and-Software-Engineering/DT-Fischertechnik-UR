package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.machines.ConveyorBelt;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;

import static com.seurteaching.digitaltwin.machines.MachineState.*;

public class ConveyorBelt01 extends SimulationBaseMachine<ConveyorBelt01.ConveyorBelt01State, ConveyorBelt> {

    private VacuumGripper02 vacuumGripper02;

    public static enum ConveyorBelt01State implements MachineState {
        Running, WaitingIncoming, WaitingOutgoing, WaitingGripper;

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }

    public ConveyorBelt01() {
        this(1.0, 1.0);
    }

    public ConveyorBelt01(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "ConveyorBelt01",
            new ConveyorBelt(
                "ConveyorBelt01",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                true,
                false
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
    }

    public void setVacuumGripper02(VacuumGripper02 vacuumGripper02) {
        this.vacuumGripper02 = vacuumGripper02;
    }

    @Override
    public void tick(ConveyorBelt01State state) throws InterruptedException {   
        switch (state) {
            case Running:
                this.baseMachine.setState(RUNNING);
                this.baseMachine.setForward(true);
                this.baseMachine.setConveyorRunning(true);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                freeze(3680);
                
                setState(ConveyorBelt01State.WaitingOutgoing);
                break;
            case WaitingIncoming:
                this.baseMachine.setState(IDLE);
                this.baseMachine.setForward(false);
                this.baseMachine.setConveyorRunning(false);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                freeze(100);

                break;
            case WaitingOutgoing:
                this.baseMachine.setState(IDLE);
                this.baseMachine.setForward(false);
                this.baseMachine.setConveyorRunning(false);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                freeze(100);

                break;
            case WaitingGripper:
                this.baseMachine.setState(IDLE);
                this.baseMachine.setForward(false);
                this.baseMachine.setConveyorRunning(false);
                this.baseMachine.updateTimestamp();
                this.baseMachine.setDirty(this.baseMachine.isDirty());
                this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                freeze(100);

                //if gripper is in hover position, move the conveyor belt (is inefficient but the pyhsical machine works like this)
                if(vacuumGripper02.getState() == VacuumGripper02.VacuumGripper02State.P4) {
                    setState(ConveyorBelt01State.Running);
                }
                break;
        }      
    }

    @Override
    public ConveyorBelt01State getDefaultState() {
        return ConveyorBelt01State.WaitingIncoming;
    }

    @Override
    protected void postReceivePacket() {
        setState(ConveyorBelt01State.WaitingGripper);
    }

    protected void postEjectPacket() {
        setState(ConveyorBelt01State.WaitingIncoming);
    }
}
