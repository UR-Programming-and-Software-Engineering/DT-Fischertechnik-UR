package com.seurteaching.digitaltwin.machines;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;

@Entity
@Table(name = "conveyor_belt")
@DiscriminatorValue("CONVEYOR")
public class ConveyorBelt extends BaseMachine {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ConveyorBelt.class);

    @Column(name = "forward_flag", nullable = false)
    private boolean forward;

    @Column(name = "conveyor_running", nullable = false)
    private boolean conveyorRunning;

    public ConveyorBelt() {
        super();
    }

    public ConveyorBelt(String machineName, MachineState state, LocalDateTime timestamp, boolean forward, boolean conveyorRunning) {
        super(machineName, state, timestamp);
        this.forward = forward;
        this.conveyorRunning = conveyorRunning;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isConveyorRunning() {
        return conveyorRunning;
    }

    public void setForward(boolean newForward) {
        this.forward = newForward;
    }

    public void setConveyorRunning(boolean newConveyorRunning) {
        this.conveyorRunning = newConveyorRunning;
    }

    public void processMQTT(String attributeName, String stringValue) {
        switch (attributeName) {
            case "conveyorActForward":
                boolean forwardValue = Boolean.parseBoolean(stringValue);
                this.setForward(forwardValue);
                break;

            case "isExecuting":
                boolean runningValue = Boolean.parseBoolean(stringValue);
                this.setConveyorRunning(runningValue);
                break;

            default:
                //logger.warn("Unknown attribute: " + attributeName);
                break;
        }
    }

    @Override
    public String toString() {
        return "ConveyorBelt [forward=" + forward + ", conveyorRunning=" + conveyorRunning + ", id=" + id
                + ", machineName=" + machineName + ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}