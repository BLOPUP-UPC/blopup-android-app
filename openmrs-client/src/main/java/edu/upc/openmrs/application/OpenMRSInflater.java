/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package edu.upc.openmrs.application;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.indicators.Indicator;

import java.util.Arrays;

import edu.upc.R;

public class OpenMRSInflater {
    private LayoutInflater mInflater;

    public OpenMRSInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public ViewGroup addKeyValueStringView(ViewGroup parentLayout, String label, String data) {
        View view = mInflater.inflate(R.layout.row_key_value_data, null, false);
        TextView labelText = view.findViewById(R.id.keyValueDataRowTextLabel);
        if (label.contains(":")) {
            labelText.setText(label.substring(0, label.indexOf(':')));
        } else {
            labelText.setText(label);
        }

        TextView dataText = view.findViewById(R.id.keyValueDataRowTextData);
        dataText.setText(data);
        parentLayout.addView(view);
        return parentLayout;
    }


    public ViewGroup addSingleStringView(ViewGroup parentLayout, String label) {
        View view = mInflater.inflate(R.layout.row_single_text_data, null, false);
        TextView labelText = view.findViewById(R.id.singleTextRowLabelText);
        labelText.setText(label);
        parentLayout.addView(view);
        return parentLayout;
    }

    public ViewGroup addBmiChart(ViewGroup parentLayout, String bmiData) {
        View view = mInflater.inflate(R.layout.bmi_chart, null, false);

        createChart(view, bmiData);

        parentLayout.addView(view);
        return parentLayout;
    }

    private void createChart(View view, String bmiData){
        SpeedView speedometer = view.findViewById(R.id.speedView);

        setIndicator(speedometer);
        setSections(speedometer);
        setValue(speedometer, bmiData);
    }

    private  void setValue(SpeedView speedometer, String bmiData){
        speedometer.speedTo(Float.parseFloat(bmiData));
        speedometer.setSpeedTextPosition(Gauge.Position.CENTER);
        speedometer.setSpeedTextColor(Color.GRAY);
        speedometer.setUnit("");
    }
    private void setIndicator(SpeedView speedometer){
        speedometer.setIndicator(Indicator.Indicators.HalfLineIndicator);
        speedometer.getIndicator().setColor(Color.BLACK);
        speedometer.setCenterCircleRadius(0f);
        speedometer.setWithTremble(false);
    }

    private void setSections(SpeedView speedometer){
        int red = Color.rgb(255, 54, 63);
        int green = Color.rgb(60,179, 113);
        int yellow = Color.rgb(255,215,0);
        int orange = Color.rgb(255, 165, 0);
        float width = speedometer.getSpeedometerWidth();

        speedometer.setStartDegree(205);
        speedometer.setEndDegree(335);
        speedometer.setTicks(Arrays.asList(0f, .25f, .375f, .50f, .75f, 1f));
        speedometer.setMinMaxSpeed(10f, 50f);
        speedometer.clearSections();
        speedometer.addSections(
                new Section(0f, .25f, red, width),
                new Section(.25f, .375f, green, width),
                new Section(.375f, .50f, yellow, width),
                new Section(.50f, .75f, orange, width),
                new Section(.75f, 1f, red, width));
    }
}
