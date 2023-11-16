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
package edu.upc.openmrs.activities.syncedpatients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentSyncedPatientsBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeInvisible
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ToastUtil
import java.net.UnknownHostException

@AndroidEntryPoint
class SyncedPatientsFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentSyncedPatientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyncedPatientsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncedPatientsBinding.inflate(inflater, container, false)

        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            syncedPatientRecyclerView.setHasFixedSize(true)
            syncedPatientRecyclerView.layoutManager = linearLayoutManager
            syncedPatientRecyclerView.adapter =
                SyncedPatientsRecyclerViewAdapter(
                    this@SyncedPatientsFragment,
                    ArrayList(),
                    viewModel
                )

            setupObserver()
            fetchSyncedPatients()

            swipeLayout.setOnRefreshListener {
                fetchSyncedPatients()
                swipeLayout.isRefreshing = false
            }
        }
        return binding.root
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showLoading()
                is Result.Success -> showPatientsList(result.data)
                else -> showError()
            }
        }
    }

    private fun fetchSyncedPatients() {
        viewModel.fetchSyncedPatients()
    }

    suspend fun fetchSyncedPatients(query: String) {
        //search remote
        viewModel.fetchSyncedPatients(query)
        if (viewModel.result.value is Result.Error && (viewModel.result.value as Result.Error).throwable.cause is UnknownHostException) {
            ToastUtil.error(getString(R.string.no_internet_connection))
        }
    }

    private fun showLoading() {
        with(binding) {
            syncedPatientsInitialProgressBar.makeInvisible()
            syncedPatientRecyclerView.makeGone()
        }
    }

    private fun showPatientsList(patients: List<Patient>) {
        with(binding) {
            syncedPatientsInitialProgressBar.makeGone()
            if (patients.isEmpty()) {
                syncedPatientRecyclerView.makeGone()
                showEmptyListText()
            } else {
                (syncedPatientRecyclerView.adapter as SyncedPatientsRecyclerViewAdapter).updateList(
                    patients
                )
                syncedPatientRecyclerView.makeVisible()
                hideEmptyListText()
            }
        }
    }

    private fun showError() {
        with(binding) {
            syncedPatientsInitialProgressBar.makeGone()
            syncedPatientRecyclerView.makeGone()
        }
        showEmptyListText()
    }

    private fun showEmptyListText() {
        binding.emptySyncedPatientList.makeVisible()
        binding.emptySyncedPatientList.text = getString(R.string.search_patient_no_result_for_query)
    }

    private fun hideEmptyListText() {
        binding.emptySyncedPatientList.makeGone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SyncedPatientsFragment {
            return SyncedPatientsFragment()
        }
    }
}
