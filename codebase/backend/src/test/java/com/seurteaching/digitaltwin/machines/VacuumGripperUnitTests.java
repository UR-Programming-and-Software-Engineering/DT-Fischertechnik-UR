package com.seurteaching.digitaltwin.machines;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class VacuumGripperUnitTests {

    @Test
    void deltaRotation_setsRotationCorrectly() {
        VacuumGripper vg = new VacuumGripper();
        vg.processMQTT("vacuumSensRotEncoderCounter", "1224");
        float expected = (float) (1224 / 61.2 * 360) % 360;
        assertThat(vg.getRotation()).isCloseTo(expected, within(0.001f));
    }

    @Test
    void deltaHeight_setsHeightCorrectly() {
        VacuumGripper vg = new VacuumGripper();
        vg.processMQTT("vacuumSensVerticalEncoderCounter", "150");
        int expected = Math.round(150 * 5.0f / 75.0f);
        assertThat(vg.getHeight()).isEqualTo(expected);
    }

    @Test
    void deltaForward_setsForwardCorrectly() {
        VacuumGripper vg = new VacuumGripper();
        vg.processMQTT("vacuumSensArmEncoderCounter", "300");
        int expected = Math.round(300 * 5.0f / 75.0f);
        assertThat(vg.getForward()).isEqualTo(expected);
    }

    @Test
    void delta_ignoresUnknownAttribute() {
        VacuumGripper vg = new VacuumGripper();
        vg.processMQTT("unknownAttribute", "123");
        assertThat(vg.getRotation()).isEqualTo(0.0f);
        assertThat(vg.getHeight()).isEqualTo(0);
        assertThat(vg.getForward()).isEqualTo(0);
    }

    @Test
    void delta_handlesInvalidNumberFormat() {
        VacuumGripper vg = new VacuumGripper();
        vg.processMQTT("vacuumSensRotEncoderCounter", "notANumber");
        assertThat(vg.getRotation()).isEqualTo(0.0f);
    }

}
