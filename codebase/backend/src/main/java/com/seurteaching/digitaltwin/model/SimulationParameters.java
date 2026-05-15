package com.seurteaching.digitaltwin.model;

import java.util.Map;
import java.util.HashMap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class SimulationParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int simulationDays;
    private int simulationHours;
    private int simulationMinutes;
    private double speedMultiplier;
    private double packageGenerationInterval;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "simulation_parameters_power_multipliers", joinColumns = @JoinColumn(name = "parameters_id"))
    @MapKeyColumn(name = "machine_name")
    @Column(name = "power_multiplier")
    private Map<String, Double> machinePowerMultipliers = new HashMap<>();

    // frontend breaks when id is presented
    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSimulationDays() {
        return simulationDays;
    }

    public void setSimulationDays(int simulationDays) {
        this.simulationDays = simulationDays;
    }

    public int getSimulationHours() {
        return simulationHours;
    }

    public void setSimulationHours(int simulationHours) {
        this.simulationHours = simulationHours;
    }

    public int getSimulationMinutes() {
        return simulationMinutes;
    }

    public void setSimulationMinutes(int simulationMinutes) {
        this.simulationMinutes = simulationMinutes;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(Double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getPackageGenerationInterval() {
        return packageGenerationInterval;
    }

    public void setPackageGenerationInterval(double packageGenerationInterval) {
        this.packageGenerationInterval = packageGenerationInterval;
    }

    public Map<String, Double> getMachinePowerMultipliers() {
        return machinePowerMultipliers;
    }

    public void setMachinePowerMultipliers(Map<String, Double> machinePowerMultipliers) {
        this.machinePowerMultipliers = machinePowerMultipliers;
    }

    @Override
    public String toString() {
        return "SimulationParameters{" +
            "id=" + id +
            ", simulationDays=" + simulationDays +
            ", simulationHours=" + simulationHours +
            ", simulationMinutes=" + simulationMinutes +
            ", speedMultiplier=" + speedMultiplier +
            ", packageGenerationInterval=" + packageGenerationInterval +
            ", machinePowerMultipliers=" + machinePowerMultipliers +
            '}';
    }
}
