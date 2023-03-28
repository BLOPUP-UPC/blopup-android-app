package edu.upc.openmrs.activities.visitdashboard;


import android.graphics.Color;
import android.view.View;

import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.indicators.Indicator;

import java.util.Arrays;

import edu.upc.R;
import edu.upc.openmrs.utilities.ChartColor;

public class BMIChartSetUp {

     public void createChart(View view, String bmiData){
        SpeedView speedometer = view.findViewById(R.id.speedView);

        setIndicator(speedometer);
        setSections(speedometer);
        setValue(speedometer, bmiData);
    }

    private  void setValue(SpeedView speedometer, String bmiData){
        speedometer.speedTo(Float.parseFloat(bmiData));
        speedometer.setSpeedTextPosition(Gauge.Position.CENTER);
        speedometer.setSpeedTextColor(Color.WHITE);
        speedometer.setUnit("");
    }
    private void setIndicator(SpeedView speedometer){
        speedometer.setIndicator(Indicator.Indicators.HalfLineIndicator);
        speedometer.getIndicator().setColor(Color.BLACK);
        speedometer.setCenterCircleRadius(0f);
        speedometer.setWithTremble(false);
    }

    private void setSections(SpeedView speedometer){
        float width = speedometer.getSpeedometerWidth();
        speedometer.setStartDegree(205);
        speedometer.setEndDegree(335);
        speedometer.setTicks(Arrays.asList(0f, .25f, .375f, .50f, .75f, 1f));
        speedometer.setMinMaxSpeed(10f, 50f);
        speedometer.clearSections();
        speedometer.addSections(
                new Section(0f, .25f, ChartColor.RED.value, width),
                new Section(.25f, .375f, ChartColor.GREEN.value, width),
                new Section(.375f, .50f, ChartColor.YELLOW.value, width),
                new Section(.50f, .75f, ChartColor.ORANGE.value, width),
                new Section(.75f, 1f, ChartColor.RED.value, width));
    }
}
