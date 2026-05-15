package com.seurteaching.digitaltwin.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "simulation_machine_data")
public class SimulationMachineData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int overheatingCounter;
    private double energy; // in Wh
    private int processedSteps;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_data_details_id")
    private SimulationDataDetails simulationDataDetails;

    public SimulationMachineData() {}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getOverheatingCounter() {
        return overheatingCounter;
    }
    public void setOverheatingCounter(int overheatingCounter) {
        this.overheatingCounter = overheatingCounter;
    }

    public double getEnergy() {
        return energy;
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public int getProcessedSteps() {
        return processedSteps;
    }
    public void setProcessedSteps(int processedSteps) {
        this.processedSteps = processedSteps;
    }

    public Long getId() {
        return id;
    }

    public SimulationDataDetails getSimulationDataDetails() {
        return simulationDataDetails;
    }

    public void setSimulationDataDetails(SimulationDataDetails simulationDataDetails) {
        this.simulationDataDetails = simulationDataDetails;
    }
}
