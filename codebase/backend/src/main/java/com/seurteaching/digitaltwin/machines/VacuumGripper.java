package com.seurteaching.digitaltwin.machines;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "vacuum_gripper")
public class VacuumGripper extends BaseMachine {

    private static final Logger logger = LoggerFactory.getLogger(VacuumGripper.class);


    @Column(name = "rotation")
    private float rotation;

    @Column(name = "height")
    private int height;

    @Column(name = "forward")
    private int forward;

    public VacuumGripper() {
        super();
    }

    public VacuumGripper(String machineName, MachineState state, LocalDateTime timestamp,
                         float rotation, int height, int forward) {
        super(machineName, state, timestamp);
        this.rotation = rotation;
        this.height = height;
        this.forward = forward;
    }

    public float getRotation() {
        return rotation;
    }

    public int getHeight() {
        return height;
    }

    public int getForward() {
        return forward;
    }

    public void deltaRotation(int rotationCount) {
        // setAngle° = setCount / 61.2 * 360
        float setAngle = (float) (rotationCount / 61.2 * 360);
        this.rotation = setAngle % 360;
    }

    public void deltaHeight(int heightCount) {
        // Distance (mm) = PulseCount × 5/75
        int setHeight = Math.round(heightCount * 5.0f / 75.0f);
        this.height = setHeight;
    }

    public void deltaForward(int forwardCount) {
        // Distance (mm) = PulseCount × 5/75
        int setForward = Math.round(forwardCount * 5.0f / 75.0f);
        this.forward = setForward;
    }

    @Override
    public void processMQTT(String attributeName, String stringValue) {
        try {
            switch (attributeName) {
                case "vacuumSensRotEncoderCounter":
                    int rotationCount = Integer.parseInt(stringValue);
                    this.deltaRotation(rotationCount);
                    break;
                    
                case "vacuumSensVerticalEncoderCounter":
                    int heightCount = Integer.parseInt(stringValue);
                    this.deltaHeight(heightCount);
                    break;
                    
                case "vacuumSensArmEncoderCounter":
                    int forwardCount = Integer.parseInt(stringValue);
                    this.deltaForward(forwardCount);
                    break;
                    
                default:
                    //logger.warn("Unknown attribute: " + attributeName);
                    break;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format for attribute '" + attributeName + 
                             "' with value '" + stringValue + "': " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "VacuumGripper [rotation=" + rotation + ", height=" + height + ", forward=" + forward
                + ", id=" + id + ", machineName=" + machineName + ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}

