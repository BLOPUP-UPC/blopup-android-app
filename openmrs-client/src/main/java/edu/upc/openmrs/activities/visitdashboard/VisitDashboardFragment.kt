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
import edu.upc.blopup.model.BloodPressureType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Visit
import edu.upc.blopup.ui.ResultUiState
import edu.upc.databinding.FragmentVisitDashboardBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.TREATMENT
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID
import edu.upc.sdk.utilities.ToastUtil
import edu.upc.sdk.utilities.ToastUtil.showLongToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.UUID


@AndroidEntryPoint
class VisitDashboardFragment : edu.upc.openmrs.activities.BaseFragment(), TreatmentListener {
    private lateinit var binding: FragmentVisitDashboardBinding
    private val viewModel: VisitDashboardViewModel by viewModels()
    private val logger = OpenmrsAndroid.getOpenMRSLogger()

    private val visitUuid: UUID by lazy {
        UUID.fromString(requireArguments().getString(VISIT_UUID)!!)
    }

    private val isNewVitals: Boolean by lazy {
        requireArguments().getBoolean(IS_NEW_VITALS)
    }

    companion object {
        fun newInstance(visitUuid: String, isNewVitals: Boolean) = VisitDashboardFragment().apply {
            arguments = bundleOf(
                Pair(VISIT_UUID, visitUuid),
                Pair(IS_NEW_VITALS, isNewVitals)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitDashboardBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        setupPatientNameObserver()
        setupVisitObserver()
        setUpTreatmentsObserver()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCurrentVisit(visitUuid)
    }

    private fun setupVisitObserver() {
        viewModel.visit.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultUiState.Loading -> {}
                is ResultUiState.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))

                is ResultUiState.Success -> result.data.runCatching {
                    recreateOptionsMenu()
                    displayVisit(this)
                    notifyDoctorIfNeeded(this)
                }.onFailure { logger.e(it.stackTraceToString()) }
            }
        }
    }

    private fun setupPatientNameObserver() {
        viewModel.patient.observe(viewLifecycleOwner) {
            setActionBarTitle(it.name.nameString)
        }
    }

    private fun displayVisit(visit: Visit) {
        setVitalsValues(visit)
        showBmiValues(visit)

        binding.bloodPressureLayout.makeVisible()
        BloodPressureChart().createChart(
            binding.visitDetailsLayout.findViewById(R.id.bp_speedview),
            visit.bloodPressureType()
        )
        setBloodPressureTypeAndRecommendation(visit.bloodPressureType())
        setBloodPressureInformationDialog()
        showAddTreatmentButton(visit)
    }

    private fun setVitalsValues(visit: Visit) {
        binding.systolicValue.text = visit.bloodPressure.systolic.toString()
        binding.diastolicValue.text = visit.bloodPressure.diastolic.toString()
        binding.pulseValue.text = visit.bloodPressure.pulse.toString()
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

    private fun showBmiValues(visit: Visit) {
        if (visit.heightCm != null) {
            binding.bmiLayout.makeVisible()
            binding.heightLayout.makeVisible()
            binding.heightValue.text = visit.heightCm.toString()
        }
        if (visit.weightKg != null) {
            binding.bmiLayout.makeVisible()
            binding.weightLayout.makeVisible()
            binding.weightValue.text = visit.weightKg.toString()
        }

        val bmiData = BMICalculator().execute(visit)
        if (bmiData != null) {
            binding.bmiChartInclude.bmiChart.makeVisible()
            BmiChart().setBMIValueAndChart(bmiData, binding.bmiLayout)
        }
    }

    private fun setUpTreatmentsObserver() {
        lifecycleScope.launch {
            viewModel.treatments.collectLatest { result ->
                val visit = if (result.first is ResultUiState.Success) (result.first as ResultUiState.Success<Visit>).data else null
                showTreatments(result.second, visit?.isActive() ?: false)
            }
        }

        viewModel.treatmentOperationsLiveData.observe(viewLifecycleOwner) { treatment ->
            when (treatment) {
                ResultType.FinalisedTreatmentSuccess -> {
                    ToastUtil.success(getString(R.string.treatment_finalised_successfully))
                }

                ResultType.FinalisedTreatmentError -> {
                    ToastUtil.error(getString(R.string.operation_error))
                }

                ResultType.RemoveTreatmentSuccess -> {
                    ToastUtil.success(getString(R.string.treatment_removed_successfully))
                }

                ResultType.RemoveTreatmentError -> {
                    ToastUtil.error(getString(R.string.operation_error))
                }

                else -> throw IllegalStateException()
            }
        }
    }

    private fun showAddTreatmentButton(visit: Visit) {
        with(binding.addTreatmentButton) {
            if (visit.isActive()) {
                this.makeVisible()
                this.setOnClickListener {
                    val intent = Intent(
                        requireContext(),
                        TreatmentActivity::class.java
                    )
                    intent.putExtra(VISIT_UUID, visitUuid.toString())
                    requireContext().startActivity(intent)
                }
            }
        }
    }

    private fun showTreatments(treatments: ResultUiState<List<Treatment>>, isVisitActive: Boolean) {
        if (treatments is ResultUiState.Success) {
            binding.loadingTreatmentsProgressBar.makeGone()
            showTreatmentList(treatments.data, isVisitActive)
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

    private fun showTreatmentList(treatments: List<Treatment>, isVisitActive: Boolean) {
        if (treatments.isNotEmpty()) {
            binding.recommendedTreatmentsLayout.makeVisible()
            val layoutManager = LinearLayoutManager(requireContext())
            binding.treatmentsVisitRecyclerView.layoutManager = layoutManager
            val treatmentAdapter = TreatmentRecyclerViewAdapter(
                requireContext(),
                isVisitActive,
                visitUuid.toString(),
                this
            )
            binding.treatmentsVisitRecyclerView.adapter = treatmentAdapter
            treatmentAdapter.updateData(treatments)
        } else {
            binding.recommendedTreatmentsLayout.makeGone()
        }
    }

    private fun notifyDoctorIfNeeded(visit: Visit) {
        if (!isNewVitals || viewModel.doctorHasBeenContacted.value == true) return

        val patientId = viewModel.patient.value?.id?.toString() ?: return

        when(visit.bloodPressureType()) {
            BloodPressureType.STAGE_II_B -> {
                showLongToast(
                    requireContext(),
                    ToastUtil.ToastType.NOTICE,
                    R.string.message_to_doctor
                )
                val bloodPressureValues = getString(
                    R.string.stage_II_b_msg,
                    visit.bloodPressure.systolic.toString(),
                    visit.bloodPressure.diastolic.toString(),
                )
                lifecycleScope.launch {
                    val result = viewModel.sendMessageToDoctor(
                        getString(
                            R.string.telegram_message,
                            patientId,
                            bloodPressureValues,
                            visit.location
                        )
                    )
                    handleContactDoctorResult(result)
                }
            }
            BloodPressureType.STAGE_II_C -> {
                binding.callToDoctorBanner.visibility = View.VISIBLE
                val bloodPressureValues = getString(
                    R.string.stage_II_c_msg,
                    visit.bloodPressure.systolic.toString(),
                    visit.bloodPressure.diastolic.toString(),
                )
                lifecycleScope.launch {
                    val result = viewModel.sendMessageToDoctor(
                        getString(
                            R.string.telegram_message,
                            patientId,
                            bloodPressureValues,
                            visit.location
                        )
                    )
                    handleContactDoctorResult(result)
                }
            }
            else -> {}
        }
    }

    private fun handleContactDoctorResult(result: Result<Boolean>) {
        viewModel.doctorHasBeenContacted(true)
        if (result is Result.Error) {
            if (result.throwable().cause is UnknownHostException) {
                ToastUtil.error(getString(R.string.no_internet_connection))
            } else {
                ToastUtil.error(getString(R.string.message_doctor_error))
            }
        }
    }

    fun endVisit() {
        lifecycleScope.launch {
            viewModel.endCurrentVisit(visitUuid).observeOnce(viewLifecycleOwner) { result ->
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
    }

    private fun setActionBarTitle(name: String) {
        (activity as VisitDashboardActivity).supportActionBar!!.apply {
            elevation = 0f
            title = name
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun recreateOptionsMenu() = requireActivity().invalidateOptionsMenu()

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        viewModel.visit.let {
            if (it.value is ResultUiState.Success && (it.value as ResultUiState.Success<Visit>).data.isActive()) {
                menuInflater.inflate(R.menu.active_visit_menu, menu)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()
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