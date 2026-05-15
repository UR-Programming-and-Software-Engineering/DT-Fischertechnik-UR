package com.seurteaching.digitaltwin.machines;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * Base class for all machines in the digital twin system.
 * Contains common attributes and methods shared by all machine types.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected Long id;

    @Column(nullable = false)
    protected String machineName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected MachineState state;

    @Column(nullable = false)
    protected LocalDateTime timestamp;

    @Transient
    private boolean isDirty = false;

    public BaseMachine() {
        this(null, MachineState.IDLE, LocalDateTime.now());
    }

    public BaseMachine(String machineName, MachineState state, LocalDateTime timestamp) {
        this.machineName = machineName;
        this.state = state;
        this.timestamp = timestamp;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public Long getId() {
        return id;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public MachineState getState() {
        return state;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setState(MachineState state) {
        this.state = state;
    }

    /*
     * Process incoming MQTT messages to update machine attributes.
     * Each subclass must implement this method to handle its specific attributes.
     */
    public abstract void processMQTT(String attributeName, String stringValue);

}