package com.seurteaching.digitaltwin.simulation.event;

public interface MachineStateListener {
    void onStateChange(MachineStateEvent event);
}