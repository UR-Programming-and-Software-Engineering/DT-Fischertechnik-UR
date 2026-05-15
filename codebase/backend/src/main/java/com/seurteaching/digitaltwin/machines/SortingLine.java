package com.seurteaching.digitaltwin.machines;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "sorting_line")
public class SortingLine extends BaseMachine {

    private static final Logger logger = LoggerFactory.getLogger(SortingLine.class);

    @Column(name = "conveyor_running")
    private boolean conveyorRunning;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_present")
    private Color colorPresent;

    @Enumerated(EnumType.STRING)
    @Column(name = "ejected_to_branch")
    private Branch ejectedToBranch;

    private static final Pattern EJECTOR_PATTERN =
            Pattern.compile("^sortingLineAct(?:Red|White|Blue)Ejector$");

    private static final Pattern COLOR_PATTERN =
            Pattern.compile("(?<=<Color\\.)(RED|BLUE|WHITE)(?=:)");

    public SortingLine() {
        super();
    }

    public SortingLine(String machineName,
                       MachineState state,
                       LocalDateTime timestamp,
                       boolean conveyorRunning,
                       Color colorPresent,
                       Branch ejectedToBranch) {
        super(machineName, state, timestamp);
        this.conveyorRunning = conveyorRunning;
        this.colorPresent = colorPresent;
        this.ejectedToBranch = ejectedToBranch;
    }

    public boolean isConveyorRunning() {
        return conveyorRunning;
    }

    public Color getColorPresent() {
        return colorPresent;
    }

    public Branch getEjectedToBranch() {
        return ejectedToBranch;
    }

    public void setColor(Color newColor){
        this.colorPresent = newColor;
    }
    public void setEjectedToBranch(Branch newBranch){
        this.ejectedToBranch = newBranch;
    }

    public void setConveyorRunning(boolean newValue) {
        this.conveyorRunning = newValue;
    }

    public void processMQTT(String attributeName, String stringValue){

        if (EJECTOR_PATTERN.matcher(attributeName).matches()) {
            attributeName = "ejectedToBranch";
        }

        switch (attributeName){
            case "ejectedToBranch":
                boolean ejectorActive = Boolean.parseBoolean(stringValue);
                if (ejectorActive){

                    if(this.colorPresent == null){
                        logger.warn("Ejector activated but colorPresent is null.");
                        break;
                    }

                    Branch newBranch = null;

                    switch (this.colorPresent){
                        case BLUE:
                            newBranch = Branch.LEFT;
                            break;
                        case RED:
                            newBranch = Branch.MIDDLE;
                            break;
                        case WHITE:
                            newBranch = Branch.RIGHT;
                            break;
                        default:
                            logger.warn("Unknown color: " + this.colorPresent);
                            break;
                    }
                    
                    this.setEjectedToBranch(newBranch);
                    }
                break;
            case "command":
                Matcher matcher = COLOR_PATTERN.matcher(stringValue);
                if(matcher.find()){
                    try {
                        Color newColor = Color.valueOf(matcher.group().toUpperCase());
                        this.setColor(newColor);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid color value: " + matcher.group());
                    }
                }
                break;
            case "colorPresent":
                try {
                    Color newColor = Color.valueOf(stringValue.toUpperCase());
                    this.setColor(newColor);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid color value: " + stringValue);
                }
                break;
            case "sortingLineActMotorConveyor":
                boolean boolValue = Boolean.valueOf(stringValue);
                this.setConveyorRunning(boolValue);
                break;
            default:
                //logger.warn("Unknown attribute: " + attributeName);
                break;
        }
    }

    @Override
    public String toString() {
        return "SortingLine [conveyorRunning=" + conveyorRunning + ", colorPresent=" + colorPresent
                + ", ejectedToBranch=" + ejectedToBranch + ", id=" + id + ", machineName=" + machineName
                + ", state=" + state + ", timestamp=" + timestamp + "]";
    }
}