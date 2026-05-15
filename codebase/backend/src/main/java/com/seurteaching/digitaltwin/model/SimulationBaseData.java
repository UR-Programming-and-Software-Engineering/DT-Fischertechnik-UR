package com.seurteaching.digitaltwin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "simulation_base_data")
@Inheritance(strategy = InheritanceType.JOINED)
public class SimulationBaseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime timeCreated;

    // switchted to time in ms => should be switched to long in the future
    // calculated by simulated time (see notion) * simulationSpeedup
    private String duration;

    private LocalDateTime timeEnded;

    // one-to-one relationship with SimulationParameters
    // eager fetch to use json serialization
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parameters_id")
    private SimulationParameters simulationParameters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public LocalDateTime getTimeEnded(){return timeEnded;}

    public void setTimeEnded(LocalDateTime timeEnded) {
        this.timeEnded = timeEnded;
    }
    public void setTimeEnded(long timeEndedMs){
        this.timeEnded = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeEndedMs), ZoneId.of("Europe/Berlin"));
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @JsonProperty("parameters")
    public SimulationParameters getSimulationParameters() {
        return simulationParameters;
    }

    @JsonProperty("parameters")
    public void setSimulationParameters(SimulationParameters simulationParameters) {
        this.simulationParameters = simulationParameters;
    }
}