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
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS
import edu.upc.sdk.utilities.NetworkUtils
import edu.upc.sdk.utilities.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.vitalsform.VitalsFormActivity
import edu.upc.databinding.FragmentVisitDashboardBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.openmrs.utilities.observeOnce

@AndroidEntryPoint
class VisitDashboardFragment : edu.upc.openmrs.activities.BaseFragment() {
    private var _binding: FragmentVisitDashboardBinding? = null
    private val binding get() = _binding!!
    private var visitExpandableListAdapter: VisitExpandableListAdapter? = null

    private val viewModel: VisitDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitDashboardBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        visitExpandableListAdapter =
            VisitExpandableListAdapter(
                requireContext(),
                emptyList()
            )
        setupAdapter()
        setupObserver()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchCurrentVisit()
    }

    private fun fetchCurrentVisit() = viewModel.fetchCurrentVisit()

    private fun setupAdapter() = with(binding) {
        visitDashboardExpList.emptyView = visitDashboardEmpty
        visitDashboardExpList.setAdapter(visitExpandableListAdapter)
        visitDashboardExpList.setGroupIndicator(null)
        visitDashboardEmpty.makeGone()
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                }
                is Result.Success -> result.data.run {
                    setActionBarTitle(patient.name.nameString)
                    recreateOptionsMenu()
                    updateEncountersList(encounters)
                }
                is Result.Error -> ToastUtil.error(getString(R.string.visit_fetching_error))
                else -> throw IllegalStateException()
            }
        })

    }

    private fun updateEncountersList(visitEncounters: List<Encounter>) = with(binding) {
        val possibleEncounterTypes = ENCOUNTER_TYPES_DISPLAYS.toHashSet()
        val displayableEncounters =
            visitEncounters.filter { possibleEncounterTypes.contains(it.encounterType?.display) }

        visitExpandableListAdapter?.updateList(displayableEncounters)

        if (displayableEncounters.isEmpty()) {
            visitDashboardEmpty.makeVisible()
            showEmptyEncountersSnackBar()
        }
    }

    fun endVisit() {
        if (!NetworkUtils.isOnline()) {
            ToastUtil.error(getString(R.string.visit_ending_not_online_error))
            return
        }
        viewModel.endCurrentVisit().observeOnce(viewLifecycleOwner, Observer { ended ->
            if (ended) requireActivity().finish()
            else ToastUtil.error(getString(R.string.visit_ending_error))
        })
    }

    private fun showEmptyEncountersSnackBar() = with(binding) {
        layoutInflater.inflate(R.layout.snackbar, null).apply {
            // Note (empty encounters) text
            findViewById<TextView>(R.id.snackbar_text).apply {
                setText(R.string.snackbar_empty_visit_list)
            }
            // Select action button text
            findViewById<TextView>(R.id.snackbar_action_button).apply {
                setText(R.string.snackbar_select)
                typeface = Typeface.createFromAsset(
                    requireActivity().assets,
                    ApplicationConstants.TypeFacePathConstants.ROBOTO_MEDIUM
                )
                setOnClickListener { startVitalsMeasurement() }
            }
        }.let {
            val snackBar =
                Snackbar.make(root, ApplicationConstants.EMPTY_STRING, Snackbar.LENGTH_INDEFINITE)
            val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
            snackBarLayout.setPadding(0, 0, 0, 0)
            snackBarLayout.addView(it, 0)
            snackBar.show()
        }
    }

    private fun setActionBarTitle(name: String) {
        (activity as VisitDashboardActivity).supportActionBar!!.title = name
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

    companion object {
        fun newInstance(visitId: Long) = VisitDashboardFragment().apply {
            arguments = bundleOf(Pair(VISIT_ID, visitId))
        }
    }
}