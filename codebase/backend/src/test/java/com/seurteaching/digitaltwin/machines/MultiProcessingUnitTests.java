package com.seurteaching.digitaltwin.machines;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class MultiProcessingUnitTests {

    @Test
    void delta_setsGripperPosition_Turntable() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensVacuumGripperAtTurntable", "true");
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Turntable);
    }

    @Test
    void delta_setsGripperPosition_Oven() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensVacuumGripperAtOven", "true");
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Oven);
    }

    @Test
    void delta_setsGripperPosition_Moving_onActGripperToOven() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingActGripperToOven", "true");
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Moving);
    }

    @Test
    void delta_setsGripperPosition_Moving_onActGripperToTurntable() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingActGripperToTurntable", "true");
        assertThat(mp.getGripperPosition()).isEqualTo(GripperPosition.Moving);
    }

    @Test
    void delta_setsTurntablePosVacuum() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensTurntablePosVacuum", "true");
        assertThat(mp.isTurntablePosVacuum()).isTrue();
        mp.processMQTT("multiProcessingSensTurntablePosVacuum", "false");
        assertThat(mp.isTurntablePosVacuum()).isFalse();
    }

    @Test
    void delta_setsTurntablePosBelt() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensTurntablePosBelt", "true");
        assertThat(mp.isTurntablePosBelt()).isTrue();
        mp.processMQTT("multiProcessingSensTurntablePosBelt", "false");
        assertThat(mp.isTurntablePosBelt()).isFalse();
    }

    @Test
    void delta_setsTurntablePosSaw() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensTurntablePosSaw", "true");
        assertThat(mp.isTurntablePosSaw()).isTrue();
        mp.processMQTT("multiProcessingSensTurntablePosSaw", "false");
        assertThat(mp.isTurntablePosSaw()).isFalse();
    }

    @Test
    void delta_setsSensOven() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingSensOven", "true");
        assertThat(mp.isSensOven()).isTrue();
        mp.processMQTT("multiProcessingSensOven", "false");
        assertThat(mp.isSensOven()).isFalse();
    }

    @Test
    void delta_setsCompressorOn() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingCompressor", "true");
        assertThat(mp.isMulttProcessingCompressorOn()).isTrue();
        mp.processMQTT("multiProcessingCompressor", "false");
        assertThat(mp.isMulttProcessingCompressorOn()).isFalse();
    }

    @Test
    void delta_setsOvenLight() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("multiProcessingOvenLight", "true");
        assertThat(mp.isOvenLight()).isTrue();
    }

    @Test
    void delta_doesNothing_onUnknownAttribute() {
        MultiProcessing mp = new MultiProcessing();
        mp.processMQTT("unknownAttribute", "true");
        assertThat(mp.getGripperPosition()).isNull();
        assertThat(mp.isTurntablePosVacuum()).isFalse();
        assertThat(mp.isTurntablePosBelt()).isFalse();
        assertThat(mp.isTurntablePosSaw()).isFalse();
        assertThat(mp.isSensOven()).isFalse();
        assertThat(mp.isMulttProcessingCompressorOn()).isFalse();
    }

}
