package com.seurteaching.digitaltwin.machines;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class SortingLineUnitTests {

    @Test
    void delta_setsColorPresent_whenColorPresentAttribute() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("colorPresent", "blue");
        assertThat(sl.getColorPresent()).isEqualTo(Color.BLUE);

        sl.processMQTT("colorPresent", "red");
        assertThat(sl.getColorPresent()).isEqualTo(Color.RED);

        sl.processMQTT("colorPresent", "white");
        assertThat(sl.getColorPresent()).isEqualTo(Color.WHITE);
    }

    @Test
    void delta_setsColorPresent_whenCommandAttribute() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("command", "<Color.BLUE:>");
        assertThat(sl.getColorPresent()).isEqualTo(Color.BLUE);

        sl.processMQTT("command", "<Color.RED:>");
        assertThat(sl.getColorPresent()).isEqualTo(Color.RED);

        sl.processMQTT("command", "<Color.WHITE:>");
        assertThat(sl.getColorPresent()).isEqualTo(Color.WHITE);
    }

    @Test
    void delta_setsEjectedToBranch_basedOnColorPresent() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("colorPresent", "blue");
        sl.processMQTT("ejectedToBranch", "true");
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.LEFT);

        sl.processMQTT("colorPresent", "red");
        sl.processMQTT("ejectedToBranch", "true");
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.MIDDLE);

        sl.processMQTT("colorPresent", "white");
        sl.processMQTT("ejectedToBranch", "true");
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.RIGHT);
    }

    @Test
    void delta_setsEjectedToBranch_toNone_whenColorUnknown() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("ejectedToBranch", "true");
        assertThat(sl.getEjectedToBranch()).isEqualTo(null);
    }

    @Test
    void delta_setsConveyorRunning_whenSortingLineActMotorConveyor() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("sortingLineActMotorConveyor", "true");
        assertThat(sl.isConveyorRunning()).isTrue();

        sl.processMQTT("sortingLineActMotorConveyor", "false");
        assertThat(sl.isConveyorRunning()).isFalse();
    }

    @Test
    void delta_ejectorPattern_setsEjectedToBranch() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("colorPresent", "blue");
        sl.processMQTT("sortingLineActBlueEjector", "true");
        assertThat(sl.getEjectedToBranch()).isEqualTo(Branch.LEFT);
    }

    @Test
    void delta_invalidColorPresent_doesNotThrow() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("colorPresent", "notacolor");
        assertThat(sl.getColorPresent()).isNull();
    }

    @Test
    void delta_ejectedToBranchWithFalse_doesNotChangeBranch() {
        SortingLine sl = new SortingLine();
        sl.processMQTT("colorPresent", "blue");
        sl.processMQTT("ejectedToBranch", "false");
        assertThat(sl.getEjectedToBranch()).isNull();
    }

}
