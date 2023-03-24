package edu.upc.openmrs.utilities;

import android.graphics.Color;

public enum ChartColor {

    RED(Color.rgb(255, 54, 63)),
    GREEN(Color.rgb(60,179, 113)),
    YELLOW(Color.rgb(255,215,0)),
    ORANGE(Color.rgb(255, 165, 0));

    public final int value;

    ChartColor(int value) {
        this.value = value;
    };
}
