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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.StringUtils.notEmpty
import edu.upc.sdk.utilities.StringUtils.notNull
import edu.upc.sdk.utilities.ToastUtil.error
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.databinding.FragmentPatientDetailsBinding
import edu.upc.openmrs.activities.addeditpatient.nationality.Nationality
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.openmrs.utilities.ImageUtils.showPatientPhoto
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible

@AndroidEntryPoint
class PatientDetailsFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentPatientDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailsBinding.inflate(inflater, null, false)

        setupObserver()
        fetchPatientDetails()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchPatientDetails()
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    when (result.operationType) {
                        PatientFetching -> showPatientDetails(result.data)
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
    }

    private fun fetchPatientDetails() {
        viewModel.fetchPatientData()
    }

    private fun showPatientDetails(patient: Patient) {
        with(binding) {
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
            if (patient.photo != null) {
                val photo = patient.resizedPhoto
                val patientName = patient.name.nameString
                patientPhoto.setImageBitmap(photo)
                patientPhoto.setOnClickListener {
                    showPatientPhoto(
                        requireContext(),
                        photo,
                        patientName
                    )
                }
            }
            patientDetailsName.text = patient.name.nameString
            val longTime = convertTime(patient.birthdate)
            if (longTime != null) {
                patientDetailsBirthDate.text = convertTime(longTime)
            }
            patient.attributes?.forEach {attribute ->
                val nationality = attribute.value?.let { Nationality.valueOf(it) }
                if(attribute.attributeType?.uuid == BuildConfig.NATIONALITY_ATTRIBUTE_TYPE_UUID){
                    patientDetailsNationality.text = nationality?.getLabel(requireContext())
                }
            }
            if (notEmpty(patient.phoneNumber)) {
                patientDetailsPhoneNumber.text = patient.phoneNumber
                patientDetailsPhoneNumber.visibility = View.VISIBLE
            }

            contactFirstName.text = patient.contact.givenName
            contactLastName.text = patient.contact.familyName
            contactPhoneNumber.text = patient.contactPhoneNumber

            patient.address?.let {
                addressDetailsStreet.text = it.addressString
                showAddressDetailsViewElement(
                    addressDetailsStateLabel,
                    addressDetailsState,
                    it.stateProvince
                )
                showAddressDetailsViewElement(
                    addressDetailsCountryLabel,
                    addressDetailsCountry,
                    it.country
                )
                showAddressDetailsViewElement(
                    addressDetailsPostalCodeLabel,
                    addressDetailsPostalCode,
                    it.postalCode
                )
                showAddressDetailsViewElement(
                    addressDetailsCityLabel,
                    addressDetailsCity,
                    it.cityVillage
                )
            }
            if (patient.isDeceased) {
                deceasedView.makeVisible()
                deceasedView.text = getString(
                    R.string.marked_patient_deceased_successfully,
                    patient.causeOfDeath.display
                )
            }
        }
    }

    private fun showAddressDetailsViewElement(
        detailsViewLabel: TextView,
        detailsView: TextView,
        detailsText: String?
    ) {
        if (notNull(detailsText) && notEmpty(detailsText)) {
            detailsView.text = detailsText
        } else {
            detailsView.makeGone()
            detailsViewLabel.makeGone()
        }
    }

    private fun setMenuTitle(nameString: String, identifier: String?) {
        (activity as PatientDashboardActivity).supportActionBar?.apply {
            title = nameString
            subtitle = "#$identifier"
        }
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
