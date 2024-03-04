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
package edu.upc.openmrs.activities.patientdashboard.diagnosis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.databinding.FragmentPatientDiagnosisBinding
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE

@AndroidEntryPoint
class PatientDiagnosisFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentPatientDiagnosisBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardDiagnosisViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPatientDiagnosisBinding.inflate(inflater, null, false)

        setupListView()
        setupObserver()
        fetchDiagnoses()

        return binding.root
    }

    private fun setupListView() {
        binding.patientDiagnosisList.emptyView = binding.emptyDiagnosisListView
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) showDiagnosesList(result.data)
        }
    }

    private fun fetchDiagnoses() {
        viewModel.fetchDiagnoses()
    }

    private fun showDiagnosesList(diagnoses: List<String>) {
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, diagnoses)
        binding.patientDiagnosisList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientId: String): PatientDiagnosisFragment {
            val fragment = PatientDiagnosisFragment()
            fragment.arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }
}
