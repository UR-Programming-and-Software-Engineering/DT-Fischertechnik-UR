package com.seurteaching.digitaltwin.simulation.restApi;

import java.util.Map;

public class PackageTrackingInformation {

    private Map<String, Integer> packagesByType;
    private double averageTimePerPackage;
    private double troughputPerHour;

    public PackageTrackingInformation() {}

    public Map<String, Integer> getPackagesByType() {
        return packagesByType;
    }

    public void setPackagesByType(Map<String, Integer> packagesByType) {
        this.packagesByType = packagesByType;
    }

    public double getAverageTimePerPackage() {
        return averageTimePerPackage;
    }

    public void setAverageTimePerPackage(double averageTimePerPackage) {
        this.averageTimePerPackage = averageTimePerPackage;
    }

    public double getTroughputPerHour() {
        return troughputPerHour;
    }

    public void setTroughputPerHour(double troughputPerHour) {
        this.troughputPerHour = troughputPerHour;
    }
}
