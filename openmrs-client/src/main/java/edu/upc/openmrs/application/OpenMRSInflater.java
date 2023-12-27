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

import static edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import edu.upc.BuildConfig;
import edu.upc.R;
import edu.upc.blopup.bloodpressure.BloodPressureType;
import edu.upc.openmrs.activities.visitdashboard.BMIChartSetUp;
import edu.upc.openmrs.activities.visitdashboard.BloodPressureInfoDialog;
import edu.upc.openmrs.activities.visitdashboard.TreatmentActivity;
import edu.upc.openmrs.activities.visitdashboard.TreatmentListener;
import edu.upc.openmrs.activities.visitdashboard.TreatmentRecyclerViewAdapter;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Treatment;
import kotlin.Pair;

public class OpenMRSInflater {
    private final LayoutInflater mInflater;

    public OpenMRSInflater(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public ViewGroup addVitalsData(ViewGroup parentLayout, Encounter encounter, String bmiData, BloodPressureType bloodPressureType, FragmentManager fragmentManager, Pair<Boolean, String> visit, List<Treatment> treatments, TreatmentListener listener) {

        View vitalsCardView = mInflater.inflate(R.layout.vitals_card, null, false);

        if (BuildConfig.SHOW_TREATMENT_TOGGLE) {
            showTreatment(vitalsCardView, encounter, visit, treatments, listener);
        }
        setBloodPressureTypeAndRecommendation(bloodPressureType, vitalsCardView);

        setVitalsValues(encounter.getObservations(), vitalsCardView);

        setBMIValueAndChart(bmiData, vitalsCardView);

        setBloodPressureInformationDialog(vitalsCardView, fragmentManager);

        parentLayout.addView(vitalsCardView);

        return parentLayout;
    }

    private void showTreatment(View vitalsCardView, Encounter encounter, Pair<Boolean, String> visit, List<Treatment> treatments, TreatmentListener listener) {
        Button addTreatmentButton = vitalsCardView.findViewById(R.id.add_treatment_button);
        if (visit.getFirst().equals(true)) {
            addTreatmentButton.setOnClickListener(view -> {
                Intent intent = new Intent(mInflater.getContext(), TreatmentActivity.class);
                intent.putExtra(VISIT_ID, encounter.getVisitID());
                mInflater.getContext().startActivity(intent);
            });
        } else {
            addTreatmentButton.setVisibility(View.GONE);
        }
        if (treatments != null && !treatments.isEmpty()) {
            vitalsCardView.findViewById(R.id.recommended_treatments_layout).setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mInflater.getContext());
            RecyclerView view = vitalsCardView.findViewById(R.id.treatmentsVisitRecyclerView);
            view.setLayoutManager(layoutManager);
            TreatmentRecyclerViewAdapter treatmentAdapter = new TreatmentRecyclerViewAdapter(mInflater.getContext(), visit, listener);
            view.setAdapter(treatmentAdapter);
            treatmentAdapter.updateData(treatments);
        }
    }

    private void setBloodPressureInformationDialog(View vitalsCardView, FragmentManager fragmentManager) {

        TextView bloodPressureInformation = vitalsCardView.findViewById(R.id.blood_pressure_info);

        bloodPressureInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BloodPressureInfoDialog dialogFragment = new BloodPressureInfoDialog();
                dialogFragment.show(fragmentManager, "BloodPressureInfoDialog");
            }
        });
    }

    private void setBloodPressureTypeAndRecommendation(BloodPressureType bloodPressureType, View vitalsCardView) {
        if (bloodPressureType != null) {
            vitalsCardView.findViewById(R.id.blood_pressure_layout).setVisibility(View.VISIBLE);
            TextView title = vitalsCardView.findViewById(R.id.blood_pressure_title);
            TextView recommendation = vitalsCardView.findViewById(R.id.blood_pressure_recommendation);
            title.setText(mInflater.getContext().getString(bloodPressureType.relatedText()));
            title.getBackground().setColorFilter(
                    ContextCompat.getColor(mInflater.getContext(), bloodPressureType.relatedColor()),
                    PorterDuff.Mode.SRC_IN
            );
            recommendation.setText(HtmlCompat.fromHtml(mInflater.getContext().getString(bloodPressureType.relatedRecommendation()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private static void setBMIValueAndChart(String bmiData, View vitalsCardView) {
        if (!Objects.equals(bmiData, "N/A")) {
            vitalsCardView.findViewById(R.id.bmi_layout).setVisibility(View.VISIBLE);
            TextView bmiValue = vitalsCardView.findViewById(R.id.bmi_value);
            bmiValue.setText(bmiData);

            BMIChartSetUp bmiChart = new BMIChartSetUp();
            bmiChart.createChart(vitalsCardView, bmiData);
        }
    }

    private void setVitalsValues(List<Observation> observations, View vitalsCardView) {
        for (Observation observation : observations) {

            TextView systolicValue = vitalsCardView.findViewById(R.id.systolic_value);
            TextView diastolicValue = vitalsCardView.findViewById(R.id.diastolic_value);
            TextView pulseValue = vitalsCardView.findViewById(R.id.pulse_value);
            TextView heightValue = vitalsCardView.findViewById(R.id.height_value);
            TextView weightValue = vitalsCardView.findViewById(R.id.weight_value);

            String formattedDisplayValue = formatValue(Objects.requireNonNull(observation.getDisplayValue()));

            if (observation.getDisplay().contains("Systolic")) {
                systolicValue.setText(formattedDisplayValue);
            } else if (observation.getDisplay().contains("Diastolic")) {
                diastolicValue.setText(formattedDisplayValue);
            } else if (observation.getDisplay().contains("Pulse")) {
                pulseValue.setText(formattedDisplayValue);
            } else if (observation.getDisplay().contains("Weight")) {
                if (!observation.getDisplayValue().isEmpty())
                    vitalsCardView.findViewById(R.id.weight_layout).setVisibility(View.VISIBLE);
                weightValue.setText(formattedDisplayValue);
            } else if (observation.getDisplay().contains("Height")) {
                if (!observation.getDisplayValue().isEmpty())
                    vitalsCardView.findViewById(R.id.height_layout).setVisibility(View.VISIBLE);
                heightValue.setText(formattedDisplayValue);
            }
        }
    }

    private String formatValue(String displayValue) {
        if (displayValue.contains(".")) {
            return displayValue.substring(0, displayValue.indexOf('.')).trim();
        } else {
            return displayValue.trim();
        }
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

    public void addSingleStringView(ViewGroup parentLayout, String label) {
        View view = mInflater.inflate(R.layout.row_single_text_data, null, false);
        TextView labelText = view.findViewById(R.id.singleTextRowLabelText);
        labelText.setText(label);
        parentLayout.addView(view);
    }
}
