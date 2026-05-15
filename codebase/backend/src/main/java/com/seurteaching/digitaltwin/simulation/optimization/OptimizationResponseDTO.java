package com.seurteaching.digitaltwin.simulation.optimization;

import com.seurteaching.digitaltwin.model.SimulationParameters;

public class OptimizationResponseDTO {

    private SimulationParameters packagesPerHourPrediction;
    private SimulationParameters energyPerHourPrediction;
    private SimulationParameters energyPerPackagePredicition;

    public SimulationParameters getPackagesPerHourPrediction() {
        return packagesPerHourPrediction;
    }

    public void setPackagesPerHourPrediction(SimulationParameters packagesPerHourPrediction) {
        this.packagesPerHourPrediction = packagesPerHourPrediction;
    }

    public SimulationParameters getEnergyPerHourPrediction() {
        return energyPerHourPrediction;
    }

    public void setEnergyPerHourPrediction(SimulationParameters energyPerHourPrediction) {
        this.energyPerHourPrediction = energyPerHourPrediction;
    }

    public SimulationParameters getEnergyPerPackagePredicition() {
        return energyPerPackagePredicition;
    }

    public void setEnergyPerPackagePredicition(SimulationParameters energyPerPackagePredicition) {
        this.energyPerPackagePredicition = energyPerPackagePredicition;
    }

}
