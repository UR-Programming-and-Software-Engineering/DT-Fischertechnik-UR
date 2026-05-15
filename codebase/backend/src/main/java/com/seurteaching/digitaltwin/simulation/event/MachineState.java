package com.seurteaching.digitaltwin.simulation.event;

import org.springframework.data.annotation.Immutable;

//immutable enum representing the state of a machine in the simulation
@Immutable
public interface MachineState {

    boolean isSameState(MachineState other);
}
