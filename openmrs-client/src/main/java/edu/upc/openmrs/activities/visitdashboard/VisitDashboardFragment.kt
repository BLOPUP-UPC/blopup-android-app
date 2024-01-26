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
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.bloodpressure.BloodPressureType
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.contactDoctorToggle
import edu.upc.blopup.vitalsform.VitalsFormActivity
import edu.upc.databinding.FragmentVisitDashboardBinding
import edu.upc.openmrs.application.OpenMRSInflater
import edu.upc.openmrs.utilities.SecretsUtils
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.Encounter
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
import kotlinx.coroutines.launch
import java.net.UnknownHostException


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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchCurrentVisit()
        setupVisitObserver()
        setUpTreatmentsObserver()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchCurrentVisit()
        setUpTreatmentsObserver()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCurrentVisit()
        setUpTreatmentsObserver()
    }

    private fun setupVisitObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                }

                is Result.Success -> result.data.run {
                    setActionBarTitle(patient.name.nameString)
                    recreateOptionsMenu()
                    val visit: Pair<Boolean, String?> = Pair(isActiveVisit(), uuid)
                    displayVisit(this)
                    contactDoctorToggle.check({ notifyDoctorIfNeeded(patient.identifier.identifier) })
                }

                is Result.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))
                else -> throw IllegalStateException()
            }
        }
    }

    private fun displayVisit(visit: Visit) {
        val openMRSInflater = OpenMRSInflater(layoutInflater)
        val encounter = visit.encounters.first()

        val bmiData = BMICalculator().execute(encounter.observations)
        val bloodPressureType = bloodPressureTypeFromEncounter(encounter)?.bloodPressureType

        setVitalsValues(encounter.observations, binding.wholeLayout)
        BloodPressureChart().createChart(
            binding.wholeLayout,
            bloodPressureType!!
        )
        setBloodPressureTypeAndRecommendation(bloodPressureType, binding.wholeLayout)

        setBloodPressureInformationDialog(binding.wholeLayout, requireFragmentManager())


//        val view = openMRSInflater.addVitalsData(
//            binding.visitDashboardExpList,
//            encounter,
//            bmiData,
//            bloodPressureTypeFromEncounter(encounter)?.bloodPressureType,
//            fragmentManager,
//            Pair(true, visit.uuid),
//            Result.Success(emptyList()),
//            this
//        )
    }

    private fun setVitalsValues(observations: List<Observation>, vitalsCardView: View) {
        for (observation in observations) {
            val systolicValue = vitalsCardView.findViewById<TextView>(R.id.systolic_value)
            val diastolicValue = vitalsCardView.findViewById<TextView>(R.id.diastolic_value)
            val pulseValue = vitalsCardView.findViewById<TextView>(R.id.pulse_value)
            val heightValue = vitalsCardView.findViewById<TextView>(R.id.height_value)
            val weightValue = vitalsCardView.findViewById<TextView>(R.id.weight_value)
            val formattedDisplayValue: String =
                formatValue(observation.displayValue!!)
            if (observation.display!!.contains("Systolic")) {
                systolicValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Diastolic")) {
                diastolicValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Pulse")) {
                pulseValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Weight")) {
                if (!observation.displayValue!!.isEmpty()) vitalsCardView.findViewById<View>(R.id.weight_layout).visibility =
                    View.VISIBLE
                weightValue.text = formattedDisplayValue
            } else if (observation.display!!.contains("Height")) {
                if (!observation.displayValue!!.isEmpty()) vitalsCardView.findViewById<View>(R.id.height_layout).visibility =
                    View.VISIBLE
                heightValue.text = formattedDisplayValue
            }
        }
    }

    private fun setBloodPressureTypeAndRecommendation(
        bloodPressureType: BloodPressureType?,
        vitalsCardView: View
    ) {
        if (bloodPressureType != null) {
            vitalsCardView.findViewById<View>(R.id.blood_pressure_layout).visibility =
                View.VISIBLE
            val title = vitalsCardView.findViewById<TextView>(R.id.blood_pressure_title)
            val recommendation =
                vitalsCardView.findViewById<TextView>(R.id.blood_pressure_recommendation)
            title.setText(requireContext().getString(bloodPressureType.relatedText()))
            title.background.setColorFilter(
                ContextCompat.getColor(requireContext(), bloodPressureType.relatedColor()),
                PorterDuff.Mode.SRC_IN
            )
            recommendation.text = HtmlCompat.fromHtml(
                requireContext().getString(bloodPressureType.relatedRecommendation()),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }

    private fun setBloodPressureInformationDialog(
        vitalsCardView: View,
        fragmentManager: FragmentManager
    ) {
        val bloodPressureInformation =
            vitalsCardView.findViewById<TextView>(R.id.blood_pressure_info)
        bloodPressureInformation.setOnClickListener {
            val dialogFragment = BloodPressureInfoDialog()
            dialogFragment.show(fragmentManager, "BloodPressureInfoDialog")
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
            val openMRSResult = if (result.isSuccess) {
                Result.Success(result.getOrElse { emptyList() })
            } else {
                result.exceptionOrNull().let { Result.Error(it!!) }
            }

            showTreatment(binding.wholeLayout, viewModel.visit?.encounters?.last()!!, Pair(viewModel.visit!!.isActiveVisit(), viewModel.visit?.uuid!!), openMRSResult, this)
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

    private fun showTreatment(
        vitalsCardView: View,
        encounter: Encounter,
        visit: Pair<Boolean, String>,
        treatments: Result<List<Treatment?>>,
        listener: TreatmentListener
    ) {
        val addTreatmentButton = vitalsCardView.findViewById<Button>(R.id.add_treatment_button)
        if (visit.first) {
            addTreatmentButton.setOnClickListener { view: View? ->
                val intent = Intent(
                    requireContext(),
                    TreatmentActivity::class.java
                )
                intent.putExtra(VISIT_ID, encounter.visitID)
                requireContext().startActivity(intent)
            }
        } else {
            addTreatmentButton.visibility = View.GONE
        }
        if (treatments is Result.Success<*>) {
            vitalsCardView.findViewById<View>(R.id.loadingTreatmentsProgressBar).visibility =
                View.GONE
            showTreatmentList(
                vitalsCardView,
                visit,
                treatments as Result.Success<List<Treatment>>,
                this
            )
        } else {
            vitalsCardView.findViewById<View>(R.id.loadingTreatmentsProgressBar).visibility =
                View.GONE
            vitalsCardView.findViewById<View>(R.id.recommended_treatments_layout).visibility =
                View.VISIBLE
            val errorMessageView = vitalsCardView.findViewById<View>(R.id.error_loading_treatments)
            errorMessageView.visibility = View.VISIBLE
            errorMessageView.setOnClickListener { view: View? ->
                vitalsCardView.findViewById<View>(R.id.loadingTreatmentsProgressBar).visibility =
                    View.VISIBLE
                errorMessageView.visibility = View.GONE
                listener.onRefreshTreatments()
            }
        }
    }

    private fun showTreatmentList(
        vitalsCardView: View,
        visit: Pair<Boolean, String>,
        treatments: Result.Success<List<Treatment>?>,
        listener: TreatmentListener
    ) {
        if (treatments.data != null && !treatments.data.isEmpty()) {
            vitalsCardView.findViewById<View>(R.id.recommended_treatments_layout).visibility =
                View.VISIBLE
            val layoutManager = LinearLayoutManager(requireContext())
            val view = vitalsCardView.findViewById<RecyclerView>(R.id.treatmentsVisitRecyclerView)
            view.layoutManager = layoutManager
            val treatmentAdapter =
                TreatmentRecyclerViewAdapter(requireContext(), visit, listener)
            view.adapter = treatmentAdapter
            treatmentAdapter.updateData(treatments.data)
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

//    private fun updateEncountersList(
//        visitEncounters: List<Encounter>,
//        visit: Pair<Boolean, String?>,
//        listener: TreatmentListener
//    ) = with(binding) {
//        visitExpandableListAdapter?.updateList(visitEncounters, visit, listener)
//        visitDashboardExpList.expandGroup(0)
//    }

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