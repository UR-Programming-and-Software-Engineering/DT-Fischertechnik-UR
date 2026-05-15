package com.seurteaching.digitaltwin.machines;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class HighbayUnitTests {

    @Test
    void delta_setsCantileverBackward() {
        Highbay hb = new Highbay();
        hb.processMQTT("cantileverBackward", "true");
        assertThat(hb.isCantileverBackward()).isTrue();
        hb.processMQTT("cantileverBackward", "false");
        assertThat(hb.isCantileverBackward()).isFalse();
    }

    @Test
    void delta_setsCantileverForward() {
        Highbay hb = new Highbay();
        hb.processMQTT("cantileverForward", "true");
        assertThat(hb.isCantileverForward()).isTrue();
        hb.processMQTT("cantileverForward", "false");
        assertThat(hb.isCantileverForward()).isFalse();
    }

    @Test
    void delta_setsCantileverUp() {
        Highbay hb = new Highbay();
        hb.processMQTT("cantileverUp", "true");
        assertThat(hb.isCantileverUp()).isTrue();
        hb.processMQTT("cantileverUp", "false");
        assertThat(hb.isCantileverUp()).isFalse();
    }

    @Test
    void delta_setsCantileverDown() {
        Highbay hb = new Highbay();
        hb.processMQTT("cantileverDown", "true");
        assertThat(hb.isCantileverDown()).isTrue();
        hb.processMQTT("cantileverDown", "false");
        assertThat(hb.isCantileverDown()).isFalse();
    }

    @Test
    void delta_setsHorizontal() {
        Highbay hb = new Highbay();
        hb.processMQTT("horizontal", "42");
        assertThat(hb.getHorizontal()).isEqualTo(42);
    }

    @Test
    void delta_setsHeight() {
        Highbay hb = new Highbay();
        hb.processMQTT("height", "123");
        assertThat(hb.getHeight()).isEqualTo(123);
    }

    @Test
    void delta_setsConveyorForward() {
        Highbay hb = new Highbay();
        hb.processMQTT("conveyorForward", "true");
        assertThat(hb.isConveyorForward()).isTrue();
        hb.processMQTT("conveyorForward", "false");
        assertThat(hb.isConveyorForward()).isFalse();
    }

    @Test
    void delta_setsConveyorBackward() {
        Highbay hb = new Highbay();
        hb.processMQTT("conveyorBackward", "true");
        assertThat(hb.isConveyorBackward()).isTrue();
        hb.processMQTT("conveyorBackward", "false");
        assertThat(hb.isConveyorBackward()).isFalse();
    }

    @Test
    void delta_doesNothing_onUnknownAttribute() {
        Highbay hb = new Highbay();
        hb.processMQTT("unknownAttribute", "true");
        // Should not throw and should not change state
        assertThat(hb.isCantileverBackward()).isFalse();
        assertThat(hb.isCantileverForward()).isFalse();
        assertThat(hb.isCantileverUp()).isFalse();
        assertThat(hb.isCantileverDown()).isFalse();
        assertThat(hb.getHorizontal()).isEqualTo(0);
        assertThat(hb.getHeight()).isEqualTo(0);
        assertThat(hb.isConveyorForward()).isFalse();
        assertThat(hb.isConveyorBackward()).isFalse();
    }

}
