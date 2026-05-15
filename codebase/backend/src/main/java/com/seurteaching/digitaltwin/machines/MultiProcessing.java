package com.seurteaching.digitaltwin.machines;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;

@Entity
@Table(name = "multi_processing")
public class MultiProcessing extends BaseMachine {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(MultiProcessing.class);

    @Column(name = "turntable_pos_saw")
    private boolean turntablePosSaw;

    @Column(name = "turntable_pos_vacuum")
    private boolean turntablePosVacuum;

    @Column(name = "turntable_pos_belt")
    private boolean turntablePosBelt;

    @Column(name = "sens_oven")
    private boolean sensOven;

    @Column(name = "oven_light")
    private boolean ovenLight;

    @Enumerated(EnumType.STRING)
    @Column(name = "gripper_position")
    private GripperPosition gripperPosition;

    @Column(name = "compressor_on")
    private boolean compressorOn;

    public MultiProcessing() {
        super();
    }

    public MultiProcessing(String machineName, MachineState state, LocalDateTime timestamp,
        boolean turntablePosSaw, boolean turntablePosVacuum, boolean turntablePosBelt,
        boolean sensOven, boolean ovenLight, GripperPosition gripperPosition, boolean compressorOn) {
        super(machineName, state, timestamp);
        this.turntablePosSaw = turntablePosSaw;
        this.turntablePosVacuum = turntablePosVacuum;
        this.turntablePosBelt = turntablePosBelt;
        this.sensOven = sensOven;
        this.ovenLight = ovenLight;
        this.gripperPosition = gripperPosition;
        this.compressorOn = compressorOn;
    }

    public GripperPosition getGripperPosition() {
        return gripperPosition;
    }

    public boolean isTurntablePosSaw() {
        return turntablePosSaw;
    }

    public boolean isTurntablePosVacuum() {
        return turntablePosVacuum;
    }

    public boolean isTurntablePosBelt() {
        return turntablePosBelt;
    }  
    
    public boolean isSensOven() {
        return sensOven;
    }

    public boolean isMulttProcessingCompressorOn() {
        return compressorOn;
    }

    public boolean isOvenLight() {
        return ovenLight;
    }
    
    public void setTurntablePosVacuum(boolean newValue) {
        this.turntablePosVacuum = newValue;
    }

    public void setTurntablePosBelt(boolean newValue) {
        this.turntablePosBelt = newValue;
    }

    public void setTurntablePosSaw(boolean newValue) {
        this.turntablePosSaw = newValue;
    }

    public void setSensOven(boolean newValue) {
        this.sensOven = newValue;
    }

    public void setCompressor(boolean newValue) {
        this.compressorOn = newValue;
    }

    public void setOvenLight(boolean newValue) {
        this.ovenLight = newValue;
    }

    public void setGripperPosition(GripperPosition newPosition) {
        this.gripperPosition = newPosition;
    }

    public void processMQTT(String attributeName, String stringValue) {

        boolean value = Boolean.parseBoolean(stringValue);

        switch (attributeName) {

            case "multiProcessingSensVacuumGripperAtTurntable":
                if(value) {
                    this.setGripperPosition(GripperPosition.Turntable);
                }
                break;
            case "multiProcessingSensVacuumGripperAtOven":
                if(value) {
                    this.setGripperPosition(GripperPosition.Oven);
                }
                break;
            case "multiProcessingActGripperToOven":
                if(value) {
                    this.setGripperPosition(GripperPosition.Moving);
                }
                break;
            case "multiProcessingActGripperToTurntable":
                if(value) {
                    this.setGripperPosition(GripperPosition.Moving);
                }
                break;
            case "multiProcessingSensTurntablePosVacuum":
                this.setTurntablePosVacuum(value);
                break;
            case "multiProcessingSensTurntablePosBelt":
                this.setTurntablePosBelt(value);
                break;
            case "multiProcessingSensTurntablePosSaw":
                this.setTurntablePosSaw(value);
                break;
            case "multiProcessingSensOven":
                this.setSensOven(value);
                break;
            case "multiProcessingCompressor":
                this.setCompressor(value);
                break;
            case "multiProcessingOvenLight":
                this.setOvenLight(value);
                break;

            default:
                //logger.warn("Unknown attribute: " + attributeName);
        }
    }

    @Override
    public String toString() {
        return "MultiProcessing [turntablePosSaw=" + turntablePosSaw + ", turntablePosVacuum=" + turntablePosVacuum
                + ", turntablePosBelt=" + turntablePosBelt + ", sensOven=" + sensOven + ", ovenLight=" + ovenLight
                + ", gripperPosition=" + gripperPosition + ", compressorOn=" + compressorOn + ", id=" + id
                + ", machineName=" + machineName + ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}