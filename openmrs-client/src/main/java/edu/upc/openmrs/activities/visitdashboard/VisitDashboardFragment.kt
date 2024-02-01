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

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.TREATMENT
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showLongToast
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.Result as KotlinResult


@AndroidEntryPoint
class VisitDashboardFragment : edu.upc.openmrs.activities.BaseFragment(), TreatmentListener {
    private var _binding: FragmentVisitDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitDashboardViewModel by viewModels()
    private val logger = OpenmrsAndroid.getOpenMRSLogger()

    companion object {
        fun newInstance(visitId: Long, isNewVitals: Boolean) = VisitDashboardFragment().apply {
            arguments = bundleOf(Pair(VISIT_UUID, visitId), Pair(IS_NEW_VITALS, isNewVitals))
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

                is Result.Success -> result.data.runCatching {
                    setActionBarTitle(patient.name.nameString)
                    recreateOptionsMenu()
                    displayVisit(this)
                    contactDoctorToggle.check({ notifyDoctorIfNeeded(patient.identifier.identifier) })
                }.onFailure { logger.e(it.stackTraceToString()) }
                is Result.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))
                else -> throw IllegalStateException()
            }
        }
    }

    private fun displayVisit(visit: Visit) {
        val encounter = visit.encounters.first()

        val bloodPressureType = bloodPressureTypeFromEncounter(encounter)?.bloodPressureType

        setVitalsValues(encounter.observations)
        showBmiChart(visit)

        bloodPressureType?.let {
            binding.bloodPressureLayout.makeVisible()
            BloodPressureChart().createChart(
                binding.visitDetailsLayout.findViewById(R.id.bp_speedview),
                bloodPressureType
            )
            setBloodPressureTypeAndRecommendation(bloodPressureType)
            setBloodPressureInformationDialog()
            showAddTreatmentButton(visit)
        }

    }

    private fun setVitalsValues(observations: List<Observation>) {
        for (observation in observations) {
            val formattedDisplayValue: String =
                formatValue(observation.displayValue!!)
            if (observation.display!!.contains("Systolic")) {
                binding.systolicValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Diastolic")) {
                binding.diastolicValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Pulse")) {
                binding.pulseValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Weight")) {
                binding.bmiLayout.makeVisible()
                if (observation.displayValue!!.isNotEmpty()) binding.weightLayout.visibility =
                    View.VISIBLE
                binding.weightValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Height")) {
                binding.bmiLayout.makeVisible()
                if (observation.displayValue!!.isNotEmpty()) binding.heightLayout.visibility =
                    View.VISIBLE
                binding.heightValue.text = formattedDisplayValue
            }
        }
    }

    private fun setBloodPressureTypeAndRecommendation(bloodPressureType: BloodPressureType?) {
        if (bloodPressureType != null) {
            binding.bloodPressureTitle.text =
                requireContext().getString(bloodPressureType.relatedText())
            binding.bloodPressureTitle.background.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    bloodPressureType.relatedColor()
                ),
                PorterDuff.Mode.SRC_IN
            )
            binding.bloodPressureRecommendation.text = HtmlCompat.fromHtml(
                requireContext().getString(bloodPressureType.relatedRecommendation()),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }

    private fun setBloodPressureInformationDialog() {
        binding.bloodPressureInfo.setOnClickListener {
            val dialogFragment = BloodPressureInfoDialog()
            dialogFragment.show(parentFragmentManager, "BloodPressureInfoDialog")
        }
    }

    private fun showBmiChart(visit: Visit) {
        val bmiData = BMICalculator().execute(visit.encounters.first().observations)
        if (!bmiData.isNullOrEmpty() && bmiData != "N/A") {
            binding.bmiChartInclude.bmiChart.makeVisible()
            BmiChart().setBMIValueAndChart(bmiData.toFloat(), binding.bmiLayout)
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

    private fun showAddTreatmentButton(visit: Visit) {
        with(binding.addTreatmentButton) {
            if (visit.isActiveVisit()) {
                this.makeVisible()
                this.setOnClickListener {
                    val intent = Intent(
                        requireContext(),
                        TreatmentActivity::class.java
                    )
                    intent.putExtra(VISIT_UUID, viewModel.visit?.uuid)
                    requireContext().startActivity(intent)
                }
            }
        }
    }

    private fun showTreatment(treatments: KotlinResult<List<Treatment>>) {
        if (treatments.isSuccess) {
            binding.loadingTreatmentsProgressBar.makeGone()
            showTreatmentList(treatments.getOrDefault(emptyList()))
        } else {
            binding.loadingTreatmentsProgressBar.makeGone()
            binding.recommendedTreatmentsLayout.makeVisible()
            val errorMessageView = binding.errorLoadingTreatments
            errorMessageView.makeVisible()

            errorMessageView.setOnClickListener {
                binding.loadingTreatmentsProgressBar.makeVisible()
                errorMessageView.makeGone()
                onRefreshTreatments()
            }
        }
    }

    private fun showTreatmentList(treatments: List<Treatment>) {
        if (treatments.isNotEmpty()) {
            binding.recommendedTreatmentsLayout.makeVisible()
            val layoutManager = LinearLayoutManager(requireContext())
            binding.treatmentsVisitRecyclerView.layoutManager = layoutManager
            val treatmentAdapter = TreatmentRecyclerViewAdapter(
                requireContext(),
                viewModel.visit?.isActiveVisit()!!,
                viewModel.visit?.uuid!!,
                this
            )
            binding.treatmentsVisitRecyclerView.adapter = treatmentAdapter
            treatmentAdapter.updateData(treatments)
        } else {
            binding.recommendedTreatmentsLayout.makeGone()
        }
    }

    private fun notifyDoctorIfNeeded(patientId: String?) {
            viewModel.bloodPressureType.observe(viewLifecycleOwner, ) { bloodPressureResult ->
                if (!requireArguments().getBoolean(IS_NEW_VITALS) || viewModel.doctorHasBeenContacted.value == true) return@observe

                if (bloodPressureResult?.bloodPressureType == BloodPressureType.STAGE_II_B) {
                    showLongToast(
                        requireContext(),
                        ToastUtil.ToastType.NOTICE,
                        R.string.message_to_doctor
                    )
                    val bloodPressureType = getString(
                        R.string.stage_II_b_msg,
                        bloodPressureResult.systolicValue.toString(),
                        bloodPressureResult.diastolicValue.toString()
                    )
                    lifecycleScope.launch {
                        val result = viewModel.sendMessageToDoctor(
                            getString(
                                R.string.telegram_message,
                                patientId,
                                bloodPressureType
                            )
                        )
                        handleContactDoctorResult(result)
                    }
                }

                if (bloodPressureResult?.bloodPressureType == BloodPressureType.STAGE_II_C) {
                    binding.callToDoctorBanner.visibility = View.VISIBLE
                    val bloodPressureType = getString(
                        R.string.stage_II_c_msg,
                        bloodPressureResult.systolicValue.toString(),
                        bloodPressureResult.diastolicValue.toString()
                    )
                    lifecycleScope.launch {
                        val result = viewModel.sendMessageToDoctor(
                            getString(
                                R.string.telegram_message,
                                patientId,
                                bloodPressureType
                            )
                        )
                        handleContactDoctorResult(result)
                    }
                }
            }
    }

    private fun handleContactDoctorResult(result: kotlin.Result<Boolean>) {
        viewModel.doctorHasBeenContacted(true)
        if (result.isFailure) {
            if (result.exceptionOrNull()?.cause is UnknownHostException) {
                ToastUtil.error(getString(R.string.no_internet_connection))
            } else {
                ToastUtil.error(getString(R.string.message_doctor_error))
            }
        }
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