package com.seurteaching.digitaltwin.machines;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class ConveyorBeltUnitTests {

    @Test
    void delta_setsForwardTrue_whenConveyorActForwardTrue() {
        ConveyorBelt cb = new ConveyorBelt();
        cb.processMQTT("conveyorActForward", "true");
        assertThat(cb.isForward()).isTrue();
    }

    @Test
    void delta_setsForwardFalse_whenConveyorActForwardFalse() {
        ConveyorBelt cb = new ConveyorBelt();
        cb.processMQTT("conveyorActForward", "false");
        assertThat(cb.isForward()).isFalse();
    }

    @Test
    void delta_setsConveyorRunningTrue_whenIsExecutingTrue() {
        ConveyorBelt cb = new ConveyorBelt();
        cb.processMQTT("isExecuting", "true");
        assertThat(cb.isConveyorRunning()).isTrue();
    }

    @Test
    void delta_setsConveyorRunningFalse_whenIsExecutingFalse() {
        ConveyorBelt cb = new ConveyorBelt();
        cb.processMQTT("isExecuting", "false");
        assertThat(cb.isConveyorRunning()).isFalse();
    }

    @Test
    void delta_doesNothing_onUnknownAttribute() {
        ConveyorBelt cb = new ConveyorBelt();
        cb.processMQTT("unknownAttribute", "true");
        assertThat(cb.isForward()).isFalse();
        assertThat(cb.isConveyorRunning()).isFalse();
    }

}
