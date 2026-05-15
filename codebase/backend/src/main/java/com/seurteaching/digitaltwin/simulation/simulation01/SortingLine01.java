package com.seurteaching.digitaltwin.simulation.simulation01;

import com.seurteaching.digitaltwin.machines.Branch;
import com.seurteaching.digitaltwin.machines.Color;
import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;
import com.seurteaching.digitaltwin.simulation.util.Packet;
import com.seurteaching.digitaltwin.machines.SortingLine;
import static com.seurteaching.digitaltwin.machines.MachineState.*;
import static com.seurteaching.digitaltwin.machines.Color.*;
import static com.seurteaching.digitaltwin.machines.Branch.*;

public class SortingLine01 extends SimulationBaseMachine<SortingLine01.SortingLine01State, SortingLine> {

    public static enum SortingLine01State implements MachineState {
        Running, WaitingIncoming, WaitingOutgoingRed, WaitingOutgoingBlue, WaitingOutgoingWhite;

        @Override
        public boolean isSameState(MachineState other) {
            return this == other;
        }
    }

    public SortingLine01() {
        this(1.0, 1.0);
    }

    public SortingLine01(double machineSpeedMultiplier) {
        this(1.0, machineSpeedMultiplier);
    }

    public SortingLine01(double speedMultiplier, double machineSpeedMultiplier) {
        super(
            "SortingLine01",
            new SortingLine(
                "SortingLine01",
                com.seurteaching.digitaltwin.machines.MachineState.IDLE,
                java.time.LocalDateTime.now(),
                true,
                Color.RED,
                Branch.RIGHT
            ),
            speedMultiplier,
            machineSpeedMultiplier
        );
    }

    public boolean isInStateWaitingOutgoing() {
        return getState()== SortingLine01State.WaitingOutgoingRed ||
               getState() == SortingLine01State.WaitingOutgoingBlue ||
               getState() == SortingLine01State.WaitingOutgoingWhite;
    }

    @Override
    public void tick(SortingLine01State state) throws InterruptedException {
                switch (state) {
                    case Running:
                        this.baseMachine.setState(RUNNING);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setConveyorRunning(true);
                        this.baseMachine.setColor(RED);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(ACTIVE_ENERGY_USAGE);

                        freeze(5280);

                        Packet packet = getPacketSnapshot();

                        if (packet.getPacketColor() == Color.RED) {
                            this.setState(SortingLine01State.WaitingOutgoingRed);
                            this.baseMachine.setState(RUNNING);
                            this.baseMachine.updateTimestamp();
                            this.baseMachine.setConveyorRunning(false);
                            this.baseMachine.setColor(RED);
                            this.baseMachine.setDirty(this.baseMachine.isDirty());
                            this.baseMachine.setEjectedToBranch(MIDDLE);
                        } else if (packet.getPacketColor() == Color.BLUE) {
                            this.setState(SortingLine01State.WaitingOutgoingBlue);
                            this.baseMachine.setState(RUNNING);
                            this.baseMachine.updateTimestamp();
                            this.baseMachine.setConveyorRunning(false);
                            this.baseMachine.setColor(BLUE);
                            this.baseMachine.setDirty(this.baseMachine.isDirty());
                            this.baseMachine.setEjectedToBranch(LEFT);
                        } else if (packet.getPacketColor() == Color.WHITE) {
                            this.setState(SortingLine01State.WaitingOutgoingWhite);
                            this.baseMachine.setState(RUNNING);
                            this.baseMachine.updateTimestamp();
                            this.baseMachine.setConveyorRunning(false);
                            this.baseMachine.setColor(WHITE);
                            this.baseMachine.setDirty(this.baseMachine.isDirty());
                            this.baseMachine.setEjectedToBranch(RIGHT);
                        }
                        break;
                    case WaitingIncoming:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setConveyorRunning(false);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);
                        break;
                    case WaitingOutgoingRed:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setConveyorRunning(false);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case WaitingOutgoingBlue:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setConveyorRunning(false);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                    case WaitingOutgoingWhite:
                        this.baseMachine.setState(IDLE);
                        this.baseMachine.updateTimestamp();
                        this.baseMachine.setConveyorRunning(false);
                        this.baseMachine.setDirty(this.baseMachine.isDirty());

                        this.energyStorage.setEnergyUsage(IDLE_ENERGY_USAGE);

                        freeze(100);

                        break;
                }
    }

    @Override
    public SortingLine01State getDefaultState() {
        return SortingLine01State.WaitingIncoming;
    }

    @Override
    protected void postReceivePacket() {
        setState(SortingLine01State.Running);
    }

}
