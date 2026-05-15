package com.seurteaching.digitaltwin.simulation.event;

import com.seurteaching.digitaltwin.simulation.SimulationBaseMachine;

public class MachineStateEvent {
    
    private final SimulationBaseMachine<?,?> machine;
    private final MachineState newState;
    private long timestamp;

    public MachineStateEvent(SimulationBaseMachine<?,?> machine, MachineState newState) {
        this.machine = machine;
        this.newState = newState;
        this.timestamp = System.currentTimeMillis();
    }

    public SimulationBaseMachine<?,?> getMachine() {
        return machine;
    }

    public MachineState getNewState() {
        return newState;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
