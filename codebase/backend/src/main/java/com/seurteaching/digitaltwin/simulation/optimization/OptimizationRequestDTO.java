package com.seurteaching.digitaltwin.simulation.optimization;

import java.util.ArrayList;

import com.seurteaching.digitaltwin.model.SimulationParameters;

public class OptimizationRequestDTO {
    private SimulationParameters parameters;
    
    private boolean fixPackageGenerationInterval;
    private ArrayList<String> fixMachinePowerMultipliers;

    public SimulationParameters getParameters() {
        return parameters;
    }

    public void setParameters(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    public ArrayList<String> getFixMachinePowerMultipliers() {
        return fixMachinePowerMultipliers;
    }

    public boolean isFixPackageGenerationInterval() {
        return fixPackageGenerationInterval;
    }
}