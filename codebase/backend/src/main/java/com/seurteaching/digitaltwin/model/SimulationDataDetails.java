package com.seurteaching.digitaltwin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Convert;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.seurteaching.digitaltwin.model.converters.DoubleListConverter;
import com.seurteaching.digitaltwin.model.converters.IntegerListConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "simulation_data_details")
public class SimulationDataDetails extends SimulationBaseData {

    private int totalPackages;
    private int expectedPackages;

    // Package throughput
    private double packageThroughputEstimated;
    private double packageThroughputReal;

    // Time per package (in seconds)
    private double timePerPackageEstimated;
    private double timePerPackageReal;

    // total energy usage per hour stored as JSON list
    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> totalEnergyPerHour = new ArrayList<>();

    // total energy usage per minute stored as JSON list
    @Convert(converter = DoubleListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Double> totalEnergyPerMinute = new ArrayList<>();

    @Convert(converter = IntegerListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Integer> hourlyPackages = new ArrayList<>();

    private double totalEnergyUsage; // in Wh

    // parameters vector for ML model input
    @Column(name = "parameters_vec")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 7)
    private float[] parametersVec;

    @JsonManagedReference
    @OneToMany(mappedBy = "simulationDataDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SimulationMachineData> machineData = new ArrayList<>();

    public float[] getParametersVec() {
        return parametersVec;
    }

    public void setParametersVec(float[] parametersVec) {
        this.parametersVec = parametersVec;
    }

    public double getTotalEnergyUsage() {
        return totalEnergyUsage;
    }

    public void setTotalEnergyUsage(double totalEnergyUsage) {
        this.totalEnergyUsage = totalEnergyUsage;
    }

    public int getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(int totalPackages) {
        this.totalPackages = totalPackages;
    }

    public int getExpectedPackages() {
        return expectedPackages;
    }

    public void setExpectedPackages(int expectedPackages) {
        this.expectedPackages = expectedPackages;
    }

    public double getPackageThroughputEstimated() {
        return packageThroughputEstimated;
    }

    public void setPackageThroughputEstimated(double packageThroughputEstimated) {
        this.packageThroughputEstimated = packageThroughputEstimated;
    }

    public double getPackageThroughputReal() {
        return packageThroughputReal;
    }

    public void setPackageThroughputReal(double packageThroughputReal) {
        this.packageThroughputReal = packageThroughputReal;
    }

    public double getTimePerPackageEstimated() {
        return timePerPackageEstimated;
    }

    public void setTimePerPackageEstimated(double timePerPackageEstimated) {
        this.timePerPackageEstimated = timePerPackageEstimated;
    }

    public double getTimePerPackageReal() {
        return timePerPackageReal;
    }

    public void setTimePerPackageReal(double timePerPackageReal) {
        this.timePerPackageReal = timePerPackageReal;
    }

    // JSON properties for nested structures expected by frontend
    @JsonProperty("packageThroughput")
    public Map<String, Double> getPackageThroughput() {
        Map<String, Double> result = new HashMap<>();
        result.put("estimated", packageThroughputEstimated);
        result.put("real", packageThroughputReal);
        return result;
    }

    @JsonProperty("timePerPackage")
    public Map<String, Double> getTimePerPackage() {
        Map<String, Double> result = new HashMap<>();
        result.put("estimated", timePerPackageEstimated);
        result.put("real", timePerPackageReal);
        return result;
    }

    public List<Double> getTotalEnergyPerHour() {
        return totalEnergyPerHour;
    }

    public void setTotalEnergyPerHour(List<Double> list) {
        this.totalEnergyPerHour = list;
    }

    public List<Double> getTotalEnergyPerMinute() {
        return totalEnergyPerMinute;
    }

    public void setTotalEnergyPerMinute(List<Double> list) {
        this.totalEnergyPerMinute = list;
    }

    public List<Integer> getHourlyPackages() {
        return hourlyPackages;
    }

    public void setHourlyPackages(List<Integer> hourlyPackages) {
        this.hourlyPackages = hourlyPackages;
    }

    /*
     * section: Machine Data Aggregations
     * These methods aggregate data from the associated SimulationMachineData entities
     * to provide summarized views for the frontend.
     */

    @JsonProperty("overheatingCounter")
    public Map<String, Integer> getOverheatingCounters() {
        if (machineData == null || machineData.isEmpty()) {
            return new HashMap<>();
        }
        return machineData.stream().collect(Collectors.toMap(
            SimulationMachineData::getName,
            SimulationMachineData::getOverheatingCounter
        ));
    }

    @JsonProperty("energyByMachine")
    public Map<String, Double> getEnergyByMachine() {
        if (machineData == null || machineData.isEmpty()) {
            return new HashMap<>();
        }
        return machineData.stream().collect(Collectors.toMap(
            SimulationMachineData::getName,
            SimulationMachineData::getEnergy
        ));
    }

    @JsonProperty("processedStepsPerMachine")
    public Map<String, Integer> getProcessedStepsPerMachine() {
        if (machineData == null || machineData.isEmpty()) {
            return new HashMap<>();
        }
        return machineData.stream().collect(Collectors.toMap(
            SimulationMachineData::getName,
            SimulationMachineData::getProcessedSteps
        ));
    }

    public List<SimulationMachineData> getMachineData() {
        return machineData;
    }

    public void setMachineData(List<SimulationMachineData> machineData) {
        this.machineData = machineData;
    }

    // store package generation interval and machine power multipliers in parametersVec
    // for ML model input
    @PrePersist
    @PreUpdate
    private void updateParametersVec() {
        if (getSimulationParameters() != null) {
            this.parametersVec = new float[] {
                (float) getSimulationParameters().getPackageGenerationInterval(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("SortingLine01", 1.0).floatValue(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("ConveyorBelt01", 1.0).floatValue(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("VacuumGripper01", 1.0).floatValue(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("VacuumGripper02", 1.0).floatValue(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("MultiProcessing01", 1.0).floatValue(),
                getSimulationParameters().getMachinePowerMultipliers().getOrDefault("HighBay01", 1.0).floatValue()
            };
        }
    }
}