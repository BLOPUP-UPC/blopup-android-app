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

package edu.upc.openmrs.activities.patientdashboard.visits;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import edu.upc.R;
import edu.upc.blopup.model.Visit;
import edu.upc.sdk.utilities.DateUtils;

public class PatientVisitsRecyclerViewAdapter extends RecyclerView.Adapter<PatientVisitsRecyclerViewAdapter.VisitViewHolder> {
    private final PatientVisitsFragment mContext;
    private List<Visit> mVisits;

    public PatientVisitsRecyclerViewAdapter(PatientVisitsFragment context, List<Visit> items) {
        this.mContext = context;
        this.mVisits = items;
    }

    @NonNull
    @Override
    public VisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_patient_visit, parent, false);
        return new VisitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitViewHolder visitViewHolder, final int position) {
        final int adapterPos = visitViewHolder.getAdapterPosition();
        Visit visit = mVisits.get(adapterPos);
        visitViewHolder.mVisitStart.setText(DateUtils.formatUsingLocale(visit.getStartDate(), Locale.getDefault()));
        if (visit.getEndDate() != null) {
            visitViewHolder.mVisitEnd.setVisibility(View.VISIBLE);
            visitViewHolder.mVisitEnd.setText(DateUtils.formatUsingLocale(visit.getEndDate(), Locale.getDefault()));

            Drawable icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.past_visit_dot, null);
            icon.setBounds(0, 0, icon.getIntrinsicHeight(), icon.getIntrinsicWidth());
            visitViewHolder.mVisitStatus.setCompoundDrawables(icon, null, null, null);
            visitViewHolder.mVisitStatus.setText(mContext.getString(R.string.past_visit_label));
        } else {
            visitViewHolder.mVisitEnd.setVisibility(View.INVISIBLE);
            Drawable icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.active_visit_dot, null);
            icon.setBounds(0, 0, icon.getIntrinsicHeight(), icon.getIntrinsicWidth());
            visitViewHolder.mVisitStatus.setCompoundDrawables(icon, null, null, null);
            visitViewHolder.mVisitStatus.setText(mContext.getString(R.string.active_visit_label));
        }
        if (visit.getLocation() != null) {
            visitViewHolder.mVisitPlace.setText(mContext.getString(R.string.visit_in, visit.getLocation()));
        }

        visitViewHolder.mCardView.setOnClickListener(view -> mContext.goToVisitDashboard(mVisits.get(adapterPos).getId()));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VisitViewHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mVisits.size();
    }

    public void updateList(List<Visit> visits) {
        mVisits = visits;
        notifyDataSetChanged();
    }

    class VisitViewHolder extends RecyclerView.ViewHolder {
        private final TextView mVisitPlace;
        private final TextView mVisitStart;
        private final TextView mVisitEnd;
        private final TextView mVisitStatus;
        private final CardView mCardView;

        public VisitViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView;
            mVisitStart = itemView.findViewById(R.id.patientVisitStartDate);
            mVisitEnd = itemView.findViewById(R.id.patientVisitEndDate);
            mVisitPlace = itemView.findViewById(R.id.patientVisitPlace);
            mVisitStatus = itemView.findViewById(R.id.visitStatusLabel);
        }

        public void clearAnimation() {
            mCardView.clearAnimation();
        }
    }
}
