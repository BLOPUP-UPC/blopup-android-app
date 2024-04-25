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

package edu.upc.openmrs.activities.patientdashboard.charts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.upc.R;

public class PatientChartsListRecyclerViewAdapter extends RecyclerView.Adapter<PatientChartsListRecyclerViewAdapter.ViewHolder> {
    private final Context mContext;
    private List<String> mVitalNameList;
    private final OnClickListener listener;

    public PatientChartsListRecyclerViewAdapter(Context mContext, JSONObject mObservationList, OnClickListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        Iterator<String> keys = mObservationList.keys();
        this.mVitalNameList = Lists.newArrayList(keys);
    }

    @NonNull
    @Override
    public PatientChartsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_vital_group, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientChartsListRecyclerViewAdapter.ViewHolder holder, int position) {
        String vitalName = mVitalNameList.get(position);
        holder.vitalName.setText(vitalName);
        holder.vitalSelector.setOnClickListener(view -> listener.showChartActivity(vitalName));
    }

    @Override
    public int getItemCount() {
        return mVitalNameList.size();
    }

    public void updateList() {
        String bloodPressureVital = mContext.getString(R.string.blood_pressure_evolution);
        mVitalNameList = Arrays.asList(bloodPressureVital);
        notifyDataSetChanged();
    }
    interface OnClickListener {
        void showChartActivity(String vitalName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView vitalName;
        final ConstraintLayout vitalSelector;

        public ViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);
            vitalName = itemView.findViewById(R.id.listVisitGroupVitalName);
            vitalSelector = itemView.findViewById(R.id.vital_list_view);
        }
    }
}
