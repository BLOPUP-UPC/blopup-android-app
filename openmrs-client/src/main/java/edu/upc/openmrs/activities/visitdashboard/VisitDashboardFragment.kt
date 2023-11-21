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
package edu.upc.openmrs.activities.visitdashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.bloodpressure.BloodPressureType
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.contactDoctorToggle
import edu.upc.blopup.vitalsform.VitalsFormActivity
import edu.upc.databinding.FragmentVisitDashboardBinding
import edu.upc.openmrs.utilities.SecretsUtils
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.NetworkUtils
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showLongToast


@AndroidEntryPoint
class VisitDashboardFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentVisitDashboardBinding? = null
    private val binding get() = _binding!!
    private var visitExpandableListAdapter: VisitExpandableListAdapter? = null
    private val viewModel: VisitDashboardViewModel by viewModels()

    companion object {
        fun newInstance(visitId: Long, isNewVitals: Boolean) = VisitDashboardFragment().apply {
            arguments = bundleOf(Pair(VISIT_ID, visitId), Pair(IS_NEW_VITALS, isNewVitals))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitDashboardBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setupExpandableListAdapter()
        setupVisitObserver()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchCurrentVisit()
    }

    private fun setupExpandableListAdapter() = with(binding) {
        visitExpandableListAdapter =
            VisitExpandableListAdapter(
                requireContext(),
                emptyList(),
                requireActivity().supportFragmentManager
            )
        visitDashboardExpList.setAdapter(visitExpandableListAdapter)
        visitDashboardExpList.setGroupIndicator(null)
    }

    private fun setupVisitObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> result.data.run {
                    setActionBarTitle(patient.name.nameString)
                    recreateOptionsMenu()
                    updateEncountersList(encounters)
                    contactDoctorToggle.check({ notifyDoctorIfNeeded(patient.identifier.identifier) })
                }

                is Result.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))
                else -> throw IllegalStateException()
            }
        }
    }

    private fun notifyDoctorIfNeeded(patientId: String?) {
        if (!requireArguments().getBoolean(IS_NEW_VITALS)) {
            return
        }

        viewModel.bloodPressureType.observe(viewLifecycleOwner) { bloodPressureResult ->
            if (bloodPressureResult?.bloodPressureType == BloodPressureType.STAGE_II_B) {
                showLongToast(
                    requireContext(),
                    ToastUtil.ToastType.NOTICE,
                    R.string.sms_to_doctor
                )
                tryToSendSMS(
                    patientId,
                    getString(
                        R.string.stage_II_b_sms,
                        bloodPressureResult.systolicValue.toString(),
                        bloodPressureResult.diastolicValue.toString()
                    )
                )
            }

            if (bloodPressureResult?.bloodPressureType == BloodPressureType.STAGE_II_C) {
                binding.callToDoctorBanner.visibility = View.VISIBLE
                tryToSendSMS(
                    patientId,
                    getString(
                        R.string.stage_II_c_sms,
                        bloodPressureResult.systolicValue.toString(),
                        bloodPressureResult.diastolicValue.toString()
                    )
                )
            }
        }

    }

    private fun tryToSendSMS(patientId: String?, bloodPressureType: String) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            showLongToast(
                requireContext(),
                ToastUtil.ToastType.ERROR,
                getString(R.string.sms_permission_denied)
            )
        } else {
            sendSms(patientId, bloodPressureType)
        }
    }

    private fun sendSms(patientId: String?, bloodPressureType: String) {
        val sm: SmsManager = if (Build.VERSION.SDK_INT >= 31) {
            requireContext().applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        val message = getString(R.string.sms_message, patientId, bloodPressureType)
        val dividedMessage = sm.divideMessage(message)
        val phoneNumber = SecretsUtils.getDoctorPhoneNumber()
        sm.sendMultipartTextMessage(phoneNumber, null, dividedMessage, null, null)
    }

    private fun updateEncountersList(visitEncounters: List<Encounter>) = with(binding) {
        visitExpandableListAdapter?.updateList(visitEncounters)
        visitDashboardExpList.expandGroup(0)
    }

    fun endVisit() {
        viewModel.endCurrentVisit().observeOnce(viewLifecycleOwner) { result ->
            when (result) {
                ResultType.Success -> {
                    requireActivity().finish()
                }

                ResultType.NoInternetError -> {
                    ToastUtil.error(getString(R.string.no_internet_connection))
                }

                else -> {
                    ToastUtil.error(getString(R.string.visit_ending_error))
                }
            }
        }
    }

    private fun setActionBarTitle(name: String) {
        (activity as VisitDashboardActivity).supportActionBar!!.apply {
            elevation = 0f
            title = name
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun startVitalsMeasurement() {
        Intent(requireActivity(), VitalsFormActivity::class.java).apply {
            putExtra(PATIENT_ID_BUNDLE, viewModel.visit?.patient?.id)
            startActivity(this)
        }
    }

    private fun recreateOptionsMenu() = requireActivity().invalidateOptionsMenu()

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        viewModel.visit?.run {
            if (isActiveVisit()) menuInflater.inflate(R.menu.active_visit_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()
            R.id.actionFillVitalsEntry -> startVitalsMeasurement()
            R.id.actionEndVisit -> edu.upc.openmrs.bundle.CustomDialogBundle().apply {
                titleViewMessage = getString(R.string.end_visit_dialog_title)
                textViewMessage = getString(R.string.end_visit_dialog_message)
                rightButtonAction =
                    edu.upc.openmrs.activities.dialog.CustomFragmentDialog.OnClickAction.END_VISIT
                rightButtonText = getString(R.string.dialog_button_ok)
                leftButtonAction =
                    edu.upc.openmrs.activities.dialog.CustomFragmentDialog.OnClickAction.DISMISS
                leftButtonText = getString(R.string.dialog_button_cancel)
            }.let {
                (requireActivity() as VisitDashboardActivity)
                    .createAndShowDialog(it, ApplicationConstants.DialogTAG.END_VISIT_DIALOG_TAG)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}