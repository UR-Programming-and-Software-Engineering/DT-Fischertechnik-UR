package com.seurteaching.digitaltwin.machines;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;

@Entity
@Table(name = "highbay")
public class Highbay extends BaseMachine{

    private static final Logger logger = LoggerFactory.getLogger(Highbay.class);

    @Column(name = "cantilever_backward")
    private boolean cantileverBackward;

    @Column(name = "cantilever_forward")
    private boolean cantileverForward;

    @Column(name = "cantilever_up")
    private boolean cantileverUp;

    @Column(name = "cantilever_down")
    private boolean cantileverDown;

    @Column(name = "horizontal_position")
    private int horizontal;

    @Column(name = "vertical_position")
    private int height;

    @Column(name = "conveyor_forward")
    private boolean conveyorForward;

    @Column(name = "conveyor_backward")
    private boolean conveyorBackward;

    public Highbay() {
        super();
    }

    public Highbay(String machineName, 
                   MachineState state, 
                   LocalDateTime timestamp,    
                   boolean cantileverBackward,
                   boolean cantileverForward,
                   boolean cantileverUp,
                   boolean cantileverDown,
                   int horizontal,
                   int height,
                   boolean conveyorForward,
                   boolean conveyorBackward) {
        super(machineName, state, timestamp);
        this.cantileverBackward = cantileverBackward;
        this.cantileverForward = cantileverForward;
        this.cantileverUp = cantileverUp;
        this.cantileverDown = cantileverDown;
        this.horizontal = horizontal;
        this.height = height;
        this.conveyorForward = conveyorForward;
        this.conveyorBackward = conveyorBackward;
    }

    public boolean isCantileverBackward() {
        return cantileverBackward;
    }

    public void setCantileverBackward(boolean backward) {
        this.cantileverBackward = backward;
    }

    public boolean isCantileverForward() {
        return cantileverForward;
    }

    public void setCantileverForward(boolean cantileverForward) {
        this.cantileverForward = cantileverForward;
    }

    public boolean isCantileverUp() {
        return cantileverUp;
    }

    public void setCantileverUp(boolean cantileverUp) {
        this.cantileverUp = cantileverUp;
    }

    public boolean isCantileverDown() {
        return cantileverDown;
    }

    public void setCantileverDown(boolean cantileverDown) {
        this.cantileverDown = cantileverDown;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isConveyorForward() {
        return conveyorForward;
    }

    public void setConveyorForward(boolean conveyorForward) {
        this.conveyorForward = conveyorForward;
    }

    public boolean isConveyorBackward() {
        return conveyorBackward;
    }

    public void setConveyorBackward(boolean conveyorBackward) {
        this.conveyorBackward = conveyorBackward;
    }

    public void processMQTT(String attributeName, String stringValue) {
        
        try {
            switch (attributeName) {
                case "cantileverBackward":
                    boolean cantileverBackwardValue = Boolean.parseBoolean(stringValue);
                    this.setCantileverBackward(cantileverBackwardValue);
                    break;

                case "cantileverForward":
                    boolean cantileverForwardValue = Boolean.parseBoolean(stringValue);
                    this.setCantileverForward(cantileverForwardValue);
                    break;

                case "cantileverUp":
                    boolean cantileverUpValue = Boolean.parseBoolean(stringValue);
                    this.setCantileverUp(cantileverUpValue);
                    break;

                case "cantileverDown":
                    boolean cantileverDownValue = Boolean.parseBoolean(stringValue);
                    this.setCantileverDown(cantileverDownValue);
                    break;

                case "horizontal":
                    int horizontalValue = Integer.parseInt(stringValue);
                    this.setHorizontal(horizontalValue);
                    break;

                case "height":
                    int heightValue = Integer.parseInt(stringValue);
                    this.setHeight(heightValue);
                    break;

                case "conveyorForward":
                    boolean conveyorForwardValue = Boolean.parseBoolean(stringValue);
                    this.setConveyorForward(conveyorForwardValue);
                    break;

                case "conveyorBackward":
                    boolean conveyorBackwardValue = Boolean.parseBoolean(stringValue);
                    this.setConveyorBackward(conveyorBackwardValue);
                    break;

                default:
                    //logger.warn("Unknown attribute: " + attributeName);
                    break;
            }

        }catch(NumberFormatException e) {
            logger.warn("Invalid number format for attribute '" + attributeName + 
                        "' with value '" + stringValue + "': " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Highbay [cantileverBackward=" + cantileverBackward + ", cantileverForward=" + cantileverForward
                + ", cantileverUp=" + cantileverUp + ", cantileverDown=" + cantileverDown + ", horizontal="
                + horizontal + ", height=" + height + ", conveyorForward=" + conveyorForward
                + ", conveyorBackward=" + conveyorBackward + ", id=" + id + ", machineName=" + machineName
                + ", state=" + state + ", timestamp=" + timestamp + "]";
    }

}




