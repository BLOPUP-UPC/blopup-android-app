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
package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.databinding.FragmentPatientChartsBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE
import org.json.JSONObject

@AndroidEntryPoint
class PatientChartsFragment : edu.upc.openmrs.activities.BaseFragment(), PatientChartsRecyclerViewAdapter.OnClickListener {
    private lateinit var binding: FragmentPatientChartsBinding

    private val patientId: Int by lazy {
        requireArguments().getString(PATIENT_ID_BUNDLE)!!.toInt()
    }
    private val patientUuud: String by lazy {
        requireArguments().getString(PATIENT_UUID_BUNDLE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPatientChartsBinding.inflate(inflater, container, false)


        setupAdapter()
        showChartsList()

        return binding.root
    }

    private fun setupAdapter() {
        with(binding.vitalList) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter =
                PatientChartsRecyclerViewAdapter(
                    activity,
                    JSONObject(),
                    this@PatientChartsFragment
                )
        }
    }

    private fun showChartsList() {
        with(binding) {
            vitalEmpty.makeGone()
            vitalList.makeVisible()
            (vitalList.adapter as PatientChartsRecyclerViewAdapter).updateList()
        }
    }

    override fun showChartActivity(vitalName: String) {
        Intent(
            activity,
            ChartsViewActivity::class.java
        ).apply {
            val bundle = Bundle().apply {
                putInt(PATIENT_ID_BUNDLE, patientId)
                putString(PATIENT_UUID_BUNDLE, patientUuud)
            }
            putExtra(ApplicationConstants.BUNDLE, bundle)
            startActivity(this)
        }
    }

    companion object {
        fun newInstance(patientId: String, patientUuid: String): PatientChartsFragment {
            val fragment = PatientChartsFragment()
            fragment.arguments = bundleOf(
                Pair(PATIENT_ID_BUNDLE, patientId),
                Pair(PATIENT_UUID_BUNDLE, patientUuid)
            )
            return fragment
        }
    }
}
