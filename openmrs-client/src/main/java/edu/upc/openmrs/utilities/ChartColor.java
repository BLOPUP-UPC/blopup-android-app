package edu.upc.openmrs.utilities;

import android.graphics.Color;

public enum ChartColor {

    BP_NORMAL(Color.parseColor("#FF03BD5B")),
    BP_HT_STAGE_I(Color.parseColor("#FFF7B334")),
    BP_HT_STAGE_II_A_AND_B(Color.parseColor("#FFED7541")),
    BP_HT_STAGE_II_C(Color.parseColor("#FFF12021"));

    public final int value;

    ChartColor(int value) {
        this.value = value;
    };
}
