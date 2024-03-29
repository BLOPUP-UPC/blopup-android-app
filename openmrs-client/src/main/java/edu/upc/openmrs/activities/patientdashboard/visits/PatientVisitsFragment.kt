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
package edu.upc.openmrs.activities.patientdashboard.visits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentPatientVisitBinding
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardActivity
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.library.models.OperationType.PatientVisitStarting
import edu.upc.sdk.library.models.OperationType.PatientVisitsFetching
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ToastUtil.error

@AndroidEntryPoint
class PatientVisitsFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentPatientVisitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardVisitsViewModel by viewModels()

    private lateinit var patientDashboardActivity: PatientDashboardActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        patientDashboardActivity = context as PatientDashboardActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPatientVisitBinding.inflate(inflater, null, false)

        setupAdapter()
        setupObserver()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchPatientVisits()
    }

    private fun setupAdapter() {
        with(binding.patientVisitRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter =
                PatientVisitsRecyclerViewAdapter(
                    this@PatientVisitsFragment,
                    emptyList()
                )
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    when (result.operationType) {
                        PatientVisitStarting -> showStartVisitProgressDialog()
                        else -> {
                        }
                    }
                }

                is Result.Success -> {
                    dismissCurrentDialog()
                    when (result.operationType) {
                        PatientVisitsFetching -> showVisitsList(result.data)
                        PatientVisitStarting -> goToVisitDashboard(result.data[0].id!!)
                        else -> {
                        }
                    }
                }

                is Result.Error -> {
                    dismissCurrentDialog()
                    when (result.operationType) {
                        PatientVisitsFetching -> showErrorFetchingVisits()
                        PatientVisitStarting -> error(getString(R.string.visit_start_error))
                        else -> {
                        }
                    }
                }

                else -> throw IllegalStateException()
            }
        }
    }

    private fun fetchPatientVisits() {
        viewModel.fetchVisitsData()
    }

    fun startVisit() {
        viewModel.startVisit()
    }

    private fun showVisitsList(visits: List<Visit>) {
        with(binding) {
            if (visits.isEmpty()) {
                patientVisitRecyclerView.makeGone()
                emptyVisitsList.makeVisible()
            } else {
                (binding.patientVisitRecyclerView.adapter as PatientVisitsRecyclerViewAdapter).updateList(visits)
                patientVisitRecyclerView.makeVisible()
                emptyVisitsList.makeGone()
            }
        }
    }

    private fun showErrorFetchingVisits() {
        binding.patientVisitRecyclerView.makeGone()
        binding.emptyVisitsList.makeVisible()
        binding.emptyVisitsList.text = getString(R.string.get_patient_from_database_error)
        error(getString(R.string.get_patient_from_database_error))
    }

    private fun showStartVisitProgressDialog() {
        patientDashboardActivity.showProgressDialog(R.string.action_starting_visit)
    }

    private fun dismissCurrentDialog() {
        patientDashboardActivity.dismissCustomFragmentDialog()
    }

    fun goToVisitDashboard(visitID: Long) {
        Intent(activity, VisitDashboardActivity::class.java).apply {
            putExtra(ApplicationConstants.BundleKeys.VISIT_UUID, visitID)
            startActivity(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.patients_visit_tab_menu, menu)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientId: String): PatientVisitsFragment {
            val fragment = PatientVisitsFragment()
            fragment.arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientId))
            return fragment
        }
    }
}
