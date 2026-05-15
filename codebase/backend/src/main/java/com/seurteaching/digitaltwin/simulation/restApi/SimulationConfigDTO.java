package com.seurteaching.digitaltwin.simulation.restApi;

import com.seurteaching.digitaltwin.model.SimulationParameters;

public class SimulationConfigDTO {
    private SimulationParameters parameters;
    private String timestamp;

    public SimulationParameters getParameters() {
        return parameters;
    }

    public void setParameters(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
