package com.seurteaching.digitaltwin.simulation.optimization;

public class PredictionResult {
    private int totalPackages;
    private double energyPerPackage;
    private double totalEnergyUsage;
    
    public PredictionResult(int totalPackages, double energyPerPackage, double totalEnergyUsage) {
        this.totalPackages = totalPackages;
        this.energyPerPackage = energyPerPackage;
        this.totalEnergyUsage = totalEnergyUsage;
    }

    public int getTotalPackages() {
        return totalPackages;
    }

    public double getEnergyPerPackage() {
        return energyPerPackage;
    }

    public double getTotalEnergyUsage() {
        return totalEnergyUsage;
    }
    
    @Override
    public String toString() {
        return String.format(
            "PredictionResult{totalPackages=%d, energyPerPackage=%.10f, totalEnergyUsage=%.10f}",
            totalPackages, energyPerPackage, totalEnergyUsage
        );
    }
}
