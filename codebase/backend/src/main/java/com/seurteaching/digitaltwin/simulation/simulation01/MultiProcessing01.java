package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.machines.GripperPosition;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.machines.MultiProcessing;
import static com.seurteaching.digitaltwin.machines.MachineState.*;

public class MultiProcessing01 extends SimulationBaseMachine<MultiProcessing01.MultiProcessing01State, MultiProcessing> {

    private VacuumGripper01 vacuumGripper01;

    public static enum MultiProcessing01State implements MachineState {
        Running, WaitingIncoming, WaitingOutgoing, WaitingGripper;

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }

    public MultiProcessing01() {
        this(1.0, 1.0);
    }

    public MultiProcessing01(double machineSpeedMultiplier) {
        this(1.0, machineSpeedMultiplier);
    }

    public MultiProcessing01(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "MultiProcessing01",
            new MultiProcessing(
                "MultiProcessing01",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                false,
                true,
                false,
                true,
                true,
                GripperPosition.Oven,
                false
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
    }

    public void setVacuumGripper01(VacuumGripper01 vacuumGripper01) {
        this.vacuumGripper01 = vacuumGripper01;
    }

    @Override
    public void tick(MultiProcessing01State state) throws InterruptedException {
                switch (state) {
                    case Running:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setTurntablePosVacuum(true);
                        this.baseMachine.setTurntablePosBelt(true);
                        this.baseMachine.setTurntablePosSaw(true);
                        this.baseMachine.setSensOven(true);
                        this.baseMachine.setCompressor(true);
                        this.baseMachine.setOvenLight(true);
                        this.baseMachine.setGripperPosition(GripperPosition.Oven);
                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(33000);

                        setState(MultiProcessing01State.WaitingOutgoing);
                        break;
                    case WaitingIncoming:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setTurntablePosVacuum(false);
                        this.baseMachine.setTurntablePosBelt(false);
                        this.baseMachine.setTurntablePosSaw(false);
                        this.baseMachine.setSensOven(false);
                        this.baseMachine.setCompressor(false);
                        this.baseMachine.setOvenLight(false);
                        this.baseMachine.setGripperPosition(GripperPosition.Oven);
                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);
                        break;
                    case WaitingOutgoing:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setTurntablePosVacuum(true);
                        this.baseMachine.setTurntablePosBelt(false);
                        this.baseMachine.setTurntablePosSaw(false);
                        this.baseMachine.setSensOven(false);
                        this.baseMachine.setCompressor(false);
                        this.baseMachine.setOvenLight(false);
                        this.baseMachine.setGripperPosition(GripperPosition.Oven);
                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);
                        break;
                    case WaitingGripper:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setTurntablePosVacuum(false);
                        this.baseMachine.setTurntablePosBelt(false);
                        this.baseMachine.setTurntablePosSaw(false);
                        this.baseMachine.setSensOven(false);
                        this.baseMachine.setCompressor(false);
                        this.baseMachine.setOvenLight(false);
                        this.baseMachine.setGripperPosition(GripperPosition.Oven);
                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        //the gripper is in the hover position and has given the packet to the multi processing machine
                        if(vacuumGripper01.getState() == VacuumGripper01.VacuumGripper01State.Idle2) {
                            setState(MultiProcessing01State.Running);
                        }
                        break;
                }      
    }

    @Override
    public MultiProcessing01State getDefaultState() {
        return MultiProcessing01State.WaitingIncoming;
    }

    @Override
    protected void postReceivePacket() {
        setState(MultiProcessing01State.WaitingGripper);
    }

    @Override
    protected void postEjectPacket() {
        setState(MultiProcessing01State.WaitingIncoming);

    }


}
