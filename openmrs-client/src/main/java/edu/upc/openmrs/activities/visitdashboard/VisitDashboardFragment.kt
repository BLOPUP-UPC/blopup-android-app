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
import android.graphics.PorterDuff
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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.bloodpressure.BloodPressureType
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.contactDoctorToggle
import edu.upc.blopup.vitalsform.VitalsFormActivity
import edu.upc.databinding.FragmentVisitDashboardBinding
import edu.upc.openmrs.utilities.SecretsUtils
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.TREATMENT
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showLongToast
import kotlinx.android.synthetic.main.bp_chart.view.bp_speedview
import kotlinx.android.synthetic.main.visit_details.view.add_treatment_button
import kotlinx.android.synthetic.main.visit_details.view.blood_pressure_info
import kotlinx.android.synthetic.main.visit_details.view.blood_pressure_layout
import kotlinx.android.synthetic.main.visit_details.view.blood_pressure_recommendation
import kotlinx.android.synthetic.main.visit_details.view.blood_pressure_title
import kotlinx.android.synthetic.main.visit_details.view.bmi_layout
import kotlinx.android.synthetic.main.visit_details.view.diastolic_value
import kotlinx.android.synthetic.main.visit_details.view.error_loading_treatments
import kotlinx.android.synthetic.main.visit_details.view.height_layout
import kotlinx.android.synthetic.main.visit_details.view.height_value
import kotlinx.android.synthetic.main.visit_details.view.loadingTreatmentsProgressBar
import kotlinx.android.synthetic.main.visit_details.view.pulse_value
import kotlinx.android.synthetic.main.visit_details.view.recommended_treatments_layout
import kotlinx.android.synthetic.main.visit_details.view.systolic_value
import kotlinx.android.synthetic.main.visit_details.view.treatmentsVisitRecyclerView
import kotlinx.android.synthetic.main.visit_details.view.weight_layout
import kotlinx.android.synthetic.main.visit_details.view.weight_value
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.Result as KotlinResult


@AndroidEntryPoint
class VisitDashboardFragment : edu.upc.openmrs.activities.BaseFragment(), TreatmentListener {
    private var _binding: FragmentVisitDashboardBinding? = null
    private val binding get() = _binding!!
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

        setupVisitObserver()
        setUpTreatmentsObserver()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCurrentVisit()
    }

    private fun setupVisitObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> result.data.run {
                    setActionBarTitle(patient.name.nameString)
                    recreateOptionsMenu()
                    displayVisit(this)
                    contactDoctorToggle.check({ notifyDoctorIfNeeded(patient.identifier.identifier) })
                }

                is Result.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))
                else -> throw IllegalStateException()
            }
        }
    }

    private fun displayVisit(visit: Visit) {
        val encounter = visit.encounters.first()

        val bloodPressureType = bloodPressureTypeFromEncounter(encounter)?.bloodPressureType

        setVitalsValues(encounter.observations)

        BloodPressureChart().createChart(
            binding.visitDetailsLayout.bp_speedview,
            bloodPressureType!!
        )

        setBloodPressureTypeAndRecommendation(bloodPressureType)

        setBloodPressureInformationDialog()

        showBmiChart()
    }

    private fun setVitalsValues(observations: List<Observation>) {
        with(binding.visitDetailsLayout) {
            for (observation in observations) {
                val formattedDisplayValue: String =
                    formatValue(observation.displayValue!!)
                if (observation.display!!.contains("Systolic")) {
                    systolic_value.text = formattedDisplayValue
                } else if (observation.display!!.contains("Diastolic")) {
                    diastolic_value.text = formattedDisplayValue
                } else if (observation.display!!.contains("Pulse")) {
                    pulse_value.text = formattedDisplayValue
                } else if (observation.display!!.contains("Weight")) {
                    if (observation.displayValue!!.isNotEmpty()) weight_layout.visibility =
                        View.VISIBLE
                    weight_value.text = formattedDisplayValue
                } else if (observation.display!!.contains("Height")) {
                    if (observation.displayValue!!.isNotEmpty()) height_layout.visibility =
                        View.VISIBLE
                    height_value.text = formattedDisplayValue
                }
            }
        }
    }

    private fun setBloodPressureTypeAndRecommendation(bloodPressureType: BloodPressureType?) {
        with(binding.visitDetailsLayout) {
            if (bloodPressureType != null) {
                blood_pressure_layout.visibility = View.VISIBLE
                blood_pressure_title.text =
                    requireContext().getString(bloodPressureType.relatedText())
                blood_pressure_title.background.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        bloodPressureType.relatedColor()
                    ),
                    PorterDuff.Mode.SRC_IN
                )
                blood_pressure_recommendation.text = HtmlCompat.fromHtml(
                    requireContext().getString(bloodPressureType.relatedRecommendation()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }

    }

    private fun setBloodPressureInformationDialog() {
        binding.visitDetailsLayout.blood_pressure_info.setOnClickListener {
            val dialogFragment = BloodPressureInfoDialog()
            dialogFragment.show(parentFragmentManager, "BloodPressureInfoDialog")
        }
    }

    private fun showBmiChart() {
        val bmiData = BMICalculator().execute(viewModel.visit?.encounters?.first()?.observations!!)
        if (!bmiData.isNullOrEmpty() && bmiData != "N/A") {
            BmiChart().setBMIValueAndChart(bmiData.toFloat(), binding.visitDetailsLayout.bmi_layout)
        }
    }

    private fun formatValue(displayValue: String): String {
        return if (displayValue.contains(".")) {
            displayValue.substring(0, displayValue.indexOf('.')).trim { it <= ' ' }
        } else {
            displayValue.trim { it <= ' ' }
        }
    }

    private fun setUpTreatmentsObserver() {
        viewModel.treatments.observe(viewLifecycleOwner) { result ->
            showTreatment(result)
        }

        viewModel.treatmentOperationsLiveData.observe(viewLifecycleOwner) { treatment ->
            when (treatment) {
                ResultType.FinalisedTreatmentSuccess -> {
                    ToastUtil.success(getString(R.string.treatment_finalised_successfully))
                }

                ResultType.FinalisedTreatmentError -> {
                    ToastUtil.error(getString(R.string.treatment_operation_error))
                }

                ResultType.RemoveTreatmentSuccess -> {
                    ToastUtil.success(getString(R.string.treatment_removed_successfully))
                }

                ResultType.RemoveTreatmentError -> {
                    ToastUtil.error(getString(R.string.treatment_operation_error))
                }

                else -> throw IllegalStateException()
            }
        }
    }

    private fun showTreatment(treatments: KotlinResult<List<Treatment>>) {

        with(binding.visitDetailsLayout.add_treatment_button) {
            if (viewModel.visit?.isActiveVisit() == true) {
                this.setOnClickListener {
                    val intent = Intent(
                        requireContext(),
                        TreatmentActivity::class.java
                    )
                    intent.putExtra(VISIT_ID, viewModel.visit?.id)
                    requireContext().startActivity(intent)
                }
            } else {
                this.visibility = View.GONE
            }
        }

        if (treatments.isSuccess) {
            binding.visitDetailsLayout.loadingTreatmentsProgressBar.visibility = View.GONE
            showTreatmentList(treatments.getOrDefault(emptyList()))
        } else {
            binding.visitDetailsLayout.loadingTreatmentsProgressBar.visibility = View.GONE
            binding.visitDetailsLayout.recommended_treatments_layout.visibility = View.VISIBLE
            val errorMessageView = binding.visitDetailsLayout.error_loading_treatments
            errorMessageView.visibility = View.VISIBLE

            errorMessageView.setOnClickListener {
                binding.visitDetailsLayout.loadingTreatmentsProgressBar.visibility = View.VISIBLE
                errorMessageView.visibility = View.GONE
                onRefreshTreatments()
            }
        }
    }

    private fun showTreatmentList(treatments: List<Treatment>) {
        if (treatments.isNotEmpty()) {
            binding.visitDetailsLayout.recommended_treatments_layout.visibility = View.VISIBLE
            val layoutManager = LinearLayoutManager(requireContext())
            binding.visitDetailsLayout.treatmentsVisitRecyclerView.layoutManager = layoutManager
            val treatmentAdapter = TreatmentRecyclerViewAdapter(requireContext(), viewModel.visit?.isActiveVisit()!!, viewModel.visit?.uuid!!, this)
            binding.visitDetailsLayout.treatmentsVisitRecyclerView.adapter = treatmentAdapter
            treatmentAdapter.updateData(treatments)
        } else {
            binding.visitDetailsLayout.recommended_treatments_layout.visibility = View.GONE
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

    fun endVisit() {
        viewModel.endCurrentVisit().observeOnce(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    requireActivity().finish()
                }

                is Result.Error -> {
                    if (result.throwable() is UnknownHostException) {
                        ToastUtil.error(getString(R.string.no_internet_connection))
                    }
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

    override fun onFinaliseClicked(treatment: Treatment) {
        lifecycleScope.launch { viewModel.finaliseTreatment(treatment) }
    }

    override fun onEditClicked(treatment: Treatment) {
        val bundle = bundleOf(TREATMENT to treatment)
        val intent = Intent(requireContext(), TreatmentActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onRemoveClicked(treatment: Treatment) {
        lifecycleScope.launch { viewModel.removeTreatment(treatment) }
    }

    override fun onRefreshTreatments() {
        lifecycleScope.launch { viewModel.refreshTreatments() }
    }
}