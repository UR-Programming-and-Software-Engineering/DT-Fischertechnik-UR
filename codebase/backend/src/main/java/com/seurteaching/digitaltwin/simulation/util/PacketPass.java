package com.seurteaching.digitaltwin.simulation.util;

import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;
import com.seurteaching.digitaltwin.simulation.event.MachineState;

public class PacketPass {
    public SimulationBaseMachine<?, ?> source;
    public SimulationBaseMachine<?, ?> target;
    public MachineState sourceState;
    public MachineState targetState;

    public PacketPass(SimulationBaseMachine<?, ?> source, SimulationBaseMachine<?, ?> target,
                      MachineState sourceState, MachineState targetState) {
        this.source = source;
        this.target = target;
        this.sourceState = sourceState;
        this.targetState = targetState;
    }
}