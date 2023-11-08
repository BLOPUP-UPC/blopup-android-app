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
package edu.upc.openmrs.activities.syncedpatients

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import edu.upc.R
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.ToastUtil

class SyncedPatientsRecyclerViewAdapter(
    private val mContext: SyncedPatientsFragment,
    private var mItems: List<Patient>,
    private val viewModel: SyncedPatientsViewModel
) :
    RecyclerView.Adapter<SyncedPatientsRecyclerViewAdapter.PatientViewHolder>() {

    fun updateList(patientList: List<Patient>) {
        mItems = patientList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_find_synced_patients, parent, false)
        return PatientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.update(mItems[position])
        val patient = mItems[position]
        if (null != patient.identifier) {
            val patientIdentifier = String.format(
                mContext.resources.getString(R.string.patient_identifier),
                patient.identifier.identifier
            )
            holder.mIdentifier.text = patientIdentifier
        }
        if (null != patient.name) {
            holder.mDisplayName.text = patient.name.nameString
        } else if (null != patient.display) {
            /* if name is null, then we can get the name from 'display' which contains the ID and name
                separated by a hyphen( - ). */
            val patientName = patient.display!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            holder.mDisplayName.text = patientName
        }
        try {
            holder.mBirthDate.text = convertTime(convertTime(patient.birthdate)!!)
        } catch (e: Exception) {
            holder.mBirthDate.text = ""
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class PatientViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val mRowLayout: CardView
        val mIdentifier: TextView
        val mDisplayName: TextView
        val mBirthDate: TextView

        init {
            mRowLayout = itemView as CardView
            mIdentifier = itemView.findViewById(R.id.syncedPatientIdentifier)
            mDisplayName = itemView.findViewById(R.id.syncedPatientDisplayName)
            mBirthDate = itemView.findViewById(R.id.syncedPatientBirthDate)
        }

        fun update(value: Patient) {
            itemView.setOnClickListener {
                val patient = viewModel.retrieveOrDownloadPatient(value.uuid)
                if (null == patient) {
                    ToastUtil.error(mContext.getString(R.string.patient_has_been_removed))
                    value.id?.let { viewModel.deletePatient(it) }
                    mItems = mItems.filter { it.uuid != value.uuid }
                    notifyDataSetChanged()
                } else {
                    val intent = Intent(mContext.activity, PatientDashboardActivity::class.java)
                    intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patient.id)
                    mContext.startActivity(intent)
                }
            }
        }
    }
}