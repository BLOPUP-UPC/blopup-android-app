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
package edu.upc.openmrs.activities.patientdashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.ui.dashboard.ActiveVisitResultUiState
import edu.upc.blopup.ui.takingvitals.VitalsActivity
import edu.upc.databinding.ActivityPatientDashboardBinding
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardActivity
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class PatientDashboardActivity : edu.upc.openmrs.activities.ACBaseActivity() {
    private lateinit var binding: ActivityPatientDashboardBinding

    private val viewModel: PatientDashboardMainViewModel by viewModels()

    private var patientId = 0L
    private lateinit var patientUuid: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(supportActionBar!!) {
            elevation = 0f
            title = getString(R.string.app_name)
        }

        intent.getLongExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, 0).also {
            if (it !== 0L) patientId = it else throw IllegalStateException("No valid patient id passed")
        }
        intent.getStringExtra(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE).also {
            if (it !== null) patientUuid = UUID.fromString(it) else  throw IllegalStateException("No valid patient uuid passed")
        }

        initViewPager()
        setupActionFABs()
    }

    private fun initViewPager() {
        val adapter = PatientDashboardPagerAdapter(supportFragmentManager, this, patientId, patientUuid.toString())
        with(binding) {
            pager.offscreenPageLimit = adapter.count - 1
            pager.adapter = adapter
            tabhost.tabMode = TabLayout.MODE_FIXED
            tabhost.setupWithViewPager(pager)

            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    binding.actionsFab.startVisitFab.isVisible = position == 0 || position == 1
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun setupActionFABs() {
        viewModel.activeVisit.observe(this) { activeVisitState ->
            binding.actionsFab.startVisitFab.show()
            when(activeVisitState) {
                ActiveVisitResultUiState.Loading -> {
                    binding.actionsFab.startVisitFab.hide()
                }
                ActiveVisitResultUiState.Error -> {
                    ToastUtil.error(getString(R.string.visit_start_error))
                }
                is ActiveVisitResultUiState.Success -> {
                    showStartVisitImpossibleDialog(title, activeVisitState.visit.id)
                }

                ActiveVisitResultUiState.NotFound -> {
                    startVitalsMeasurement()
                }
            }
        }
        with(binding.actionsFab) {
            startVisitFab.setOnClickListener {
                viewModel.fetchActiveVisit(patientUuid)
            }
        }
    }

    fun endActiveVisit(visitUuid: UUID) {
        lifecycleScope.launch {
            viewModel.endActiveVisit(visitUuid).observe(this@PatientDashboardActivity) { visitEnded ->
                if (visitEnded) {
                    startVitalsMeasurement()
                }
            }
        }
    }

    private fun startVitalsMeasurement() {
        vitalsFormLauncher.launch(
            Intent(this, VitalsActivity::class.java)
                .putExtra(
                    ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                    patientId
                )
                .putExtra(
                    ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE,
                    patientUuid.toString()
                )
        )
    }

    private val vitalsFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(
                Intent(this, VisitDashboardActivity::class.java)
                    .putExtra(ApplicationConstants.BundleKeys.VISIT_UUID, result.data?.getStringExtra(ApplicationConstants.BundleKeys.VISIT_UUID))
                    .putExtra(ApplicationConstants.BundleKeys.IS_NEW_VITALS, true)
            )

        }
    }
}
