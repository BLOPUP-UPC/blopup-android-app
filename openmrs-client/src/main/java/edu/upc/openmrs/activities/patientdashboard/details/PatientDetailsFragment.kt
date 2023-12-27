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
package edu.upc.openmrs.activities.patientdashboard.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.BuildConfig
import edu.upc.BuildConfig.SHOW_TREATMENT_TOGGLE
import edu.upc.R
import edu.upc.databinding.FragmentPatientDetailsBinding
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.openmrs.activities.visitdashboard.TreatmentRecyclerViewAdapter
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.ToastUtil.error
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PatientDetailsFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentPatientDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var treatmentAdapter: TreatmentRecyclerViewAdapter

    private val viewModel: PatientDashboardDetailsViewModel by viewModels()
    private val treatmentViewModel: TreatmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailsBinding.inflate(inflater, null, false)

        setupObservers()
        fetchPatientDetails()

        if(SHOW_TREATMENT_TOGGLE) {
            setUpActiveTreatmentsAdapter()
        } else {
            binding.recommendedTreatmentsLayout.visibility = View.GONE
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchPatientDetails()
    }

    private fun setupObservers() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> {
                    when (result.operationType) {
                        PatientFetching -> {
                            showPatientDetails(result.data)
                            lifecycleScope.launch { treatmentViewModel.fetchActiveTreatments(result.data)}
                        }
                        else -> {
                        }
                    }
                }

                is Result.Error -> {
                    when (result.operationType) {
                        PatientFetching -> error(getString(R.string.get_patient_from_database_error))
                        else -> {
                        }
                    }
                }

                else -> throw IllegalStateException()
            }

        })

        treatmentViewModel.activeTreatments.observe(viewLifecycleOwner) { treatments ->
            if(treatments.isNotEmpty()) {
                binding.recommendedTreatmentsLayout.visibility = View.VISIBLE
            } else {
                binding.recommendedTreatmentsLayout.visibility = View.GONE
            }
            treatmentAdapter.updateData(treatments)
        }
    }

    private fun fetchPatientDetails() {
        viewModel.fetchPatientData()
    }

    private fun showPatientDetails(patient: Patient) {
        with(binding) {
            edit.setOnClickListener {
                startPatientUpdateActivity(patient.id)
            }

            setMenuTitle(patient.name.nameString, patient.identifier.identifier!!)
            if (isAdded) {
                when (patient.gender) {
                    "M" -> {
                        patientDetailsGender.text = getString(R.string.male)
                    }

                    "F" -> {
                        patientDetailsGender.text = getString(R.string.female)
                    }

                    else -> {
                        patientDetailsGender.text = getString(R.string.non_binary)
                    }
                }
            }

            patientDetailsName.text = patient.name.nameString
            val longTime = convertTime(patient.birthdate)
            if (longTime != null) {
                patientDetailsBirthDate.text = convertTime(longTime)
            }
            patient.attributes?.forEach { attribute ->
                val countryOfBirthValue = attribute.value?.uppercase()
                if (attribute.attributeType?.uuid == BuildConfig.COUNTRY_OF_BIRTH_ATTRIBUTE_TYPE_UUID) {
                    val country = countryOfBirthValue?.let { Country.valueOf(it) }
                    if (country != null) {
                        patientDetailsCountryOfBirth.text = country.getLabel(requireContext())
                    }
                }
            }
        }
    }

    private fun startPatientUpdateActivity(patientId: Long?) {
        val intent = Intent(requireContext(), AddEditPatientActivity::class.java)
            .putExtra(PATIENT_ID_BUNDLE, patientId.toString())
        startActivity(intent)
    }

    private fun setMenuTitle(nameString: String, identifier: String?) {
        (activity as PatientDashboardActivity).supportActionBar?.apply {
            title = nameString
            subtitle = "#$identifier"
        }
    }

    private fun setUpActiveTreatmentsAdapter() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.treatmentsRecyclerView.layoutManager = linearLayoutManager
        treatmentAdapter = TreatmentRecyclerViewAdapter(requireContext(), Pair(false, ""), null)
        binding.treatmentsRecyclerView.adapter = treatmentAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientId: String): PatientDetailsFragment {
            val fragment = PatientDetailsFragment()
            fragment.arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }
}
