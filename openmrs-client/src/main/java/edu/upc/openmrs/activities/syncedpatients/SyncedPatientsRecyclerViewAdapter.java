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

package edu.upc.openmrs.activities.syncedpatients;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.upc.R;
import edu.upc.openmrs.activities.ACBaseActivity;
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity;
import edu.upc.sdk.library.api.repository.PatientRepository;
import edu.upc.sdk.library.api.repository.VisitRepository;
import edu.upc.sdk.library.dao.PatientDAO;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.DateUtils;

public class SyncedPatientsRecyclerViewAdapter extends RecyclerView.Adapter<SyncedPatientsRecyclerViewAdapter.PatientViewHolder> {
    private SyncedPatientsFragment mContext;
    private List<Patient> mItems;
    private boolean multiSelect = false;
    private ArrayList<Patient> selectedItems = new ArrayList<>();
    private androidx.appcompat.view.ActionMode.Callback actionModeCallbacks = new androidx.appcompat.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            multiSelect = true;
            mode.getMenuInflater().inflate(R.menu.delete_multi_patient_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            ((ACBaseActivity) mContext.requireActivity()).showMultiDeletePatientDialog(selectedItems);
            return true;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();
        }
    };

    public SyncedPatientsRecyclerViewAdapter(SyncedPatientsFragment context, List<Patient> items) {
        this.mContext = context;
        this.mItems = items;
    }

    public void updateList(List<Patient> patientList) {
        this.mItems = patientList;
        this.selectedItems = new ArrayList();
        this.multiSelect = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SyncedPatientsRecyclerViewAdapter.PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_find_synced_patients, parent, false);
        return new PatientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SyncedPatientsRecyclerViewAdapter.PatientViewHolder holder, final int position) {
        holder.update(mItems.get(position));

        final Patient patient = mItems.get(position);

        if (null != patient.getIdentifier()) {
            String patientIdentifier = String.format(mContext.getResources().getString(R.string.patient_identifier),
                    patient.getIdentifier().getIdentifier());
            holder.mIdentifier.setText(patientIdentifier);
        }
        if (null != patient.getName()) {
            holder.mDisplayName.setText(patient.getName().getNameString());
        } else if (null != patient.getDisplay()) {
                /* if name is null, then we can get the name from 'display' which contains the ID and name
                separated by a hyphen( - ). */
            String patientName = patient.getDisplay().split("-")[1];
            holder.mDisplayName.setText(patientName);

        }
        try {
            holder.mBirthDate.setText(DateUtils.convertTime(DateUtils.convertTime(patient.getBirthdate())));
        } catch (Exception e) {
            holder.mBirthDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        private CardView mRowLayout;
        private TextView mIdentifier;
        private TextView mDisplayName;
        private ImageView mGender;
        private TextView mBirthDate;
        private ColorStateList cardBackgroundColor;

        public PatientViewHolder(View itemView) {
            super(itemView);
            mRowLayout = (CardView) itemView;
            mIdentifier = itemView.findViewById(R.id.syncedPatientIdentifier);
            mDisplayName = itemView.findViewById(R.id.syncedPatientDisplayName);
            mGender = itemView.findViewById(R.id.syncedPatientGender);
            mBirthDate = itemView.findViewById(R.id.syncedPatientBirthDate);

            cardBackgroundColor = mRowLayout.getCardBackgroundColor();
        }

        void selectItem(Patient item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    mRowLayout.setCardBackgroundColor(cardBackgroundColor);
                } else {
                    selectedItems.add(item);
                    mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected_card));
                }
            }
        }

        void update(final Patient value) {
            if (selectedItems.contains(value)) {
                mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected_card));
            } else {
                mRowLayout.setCardBackgroundColor(cardBackgroundColor);
            }
            itemView.setOnLongClickListener(view -> {
                ((AppCompatActivity) mContext.requireActivity()).startSupportActionMode(actionModeCallbacks);
                selectItem(value);
                return true;
            });
            itemView.setOnClickListener(view -> {
                if (!multiSelect) {
                    Intent intent = new Intent(mContext.getActivity(), PatientDashboardActivity.class);
                    if (value.getId() == null) {
                        Patient patient = retrieveOrDownloadPatient(value.getUuid());
                        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patient.getId());
                    } else
                        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, value.getId());
                    mContext.startActivity(intent);
                } else {
                    selectItem(value);
                }
            });
        }

        private Patient retrieveOrDownloadPatient(final String patientUuid) {
            PatientDAO patientDAO = new PatientDAO();
            PatientRepository patientRepository = new PatientRepository();


            //region == Check if it exist in the db before attempting a download ==
            Patient patient = patientDAO.findPatientByUUID(patientUuid);
            if (patient != null)
                return patient;
            //endregion

            //region == Download Patient From Remote & Save To DB ==
            patient = patientRepository.downloadPatientByUuid(patientUuid)
                    .single()
                    .toBlocking()
                    .first();

            Long id = patientDAO.savePatient(patient)
                    .single()
                    .toBlocking()
                    .first();

            patient.setId(id);

            new VisitRepository().syncVisitsData(patient);
            new VisitRepository().syncLastVitals(patientUuid);
            //endregion

            return patient;
        }

    }
}
