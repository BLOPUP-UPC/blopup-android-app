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
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.vitalsform.VitalsFormActivity
import edu.upc.databinding.ActivityPatientDashboardBinding
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.OperationType.PatientSynchronizing
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.NetworkUtils
import edu.upc.sdk.utilities.ToastUtil

@AndroidEntryPoint
class PatientDashboardActivity : edu.upc.openmrs.activities.ACBaseActivity() {
    private var _binding: ActivityPatientDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardMainViewModel by viewModels()

    private lateinit var patientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(supportActionBar!!) {
            elevation = 0f
            title = getString(R.string.app_name)
        }

        patientId = viewModel.patientId

        setupObserver()
        setupActionFABs()
        if (NetworkUtils.isOnline()) {
                viewModel.syncPatientData()
        } else {
            initViewPager()
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(this, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    when (result.operationType) {
                        PatientSynchronizing -> showProgressDialog(R.string.action_synchronize_patients)
                        else -> {
                        }
                    }
                }

                is Result.Success -> {
                    dismissCustomFragmentDialog()
                    when (result.operationType) {
                        PatientSynchronizing -> {
                            ToastUtil.success(getString(R.string.synchronize_patient_successful))
                            initViewPager()
                        }
                        else -> {
                        }
                    }
                }

                is Result.Error -> {
                    dismissCustomFragmentDialog()
                    when (result.operationType) {
                        PatientSynchronizing -> {
                            ToastUtil.error(getString(R.string.synchronize_patient_error))
                            initViewPager()
                        }
                        else -> {
                        }
                    }
                }

                else -> throw IllegalStateException()
            }
        })
    }

    private fun syncPatient() {
        if (NetworkUtils.isOnline()) viewModel.syncPatientData()
        else ToastUtil.notify(getString(R.string.synchronize_patient_network_error))
    }

    private fun initViewPager() {
        val adapter = PatientDashboardPagerAdapter(supportFragmentManager, this, patientId)
        with(binding) {
            pager.offscreenPageLimit = adapter.count - 1
            pager.adapter = adapter
            tabhost.setupWithViewPager(pager)
            setUpStartVisitFAB()
            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0 || position == 2) {
                        setUpStartVisitFAB()
                    } else {
                        binding.actionsFab.startVisitFab.isVisible = false
                    }
                }
                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun setupActionFABs() {
        with(binding.actionsFab) {
            startVisitFab.setOnClickListener {
                viewModel.hasActiveVisit()
                    .observeOnce(it.findViewTreeLifecycleOwner()!!) { hasActiveVisit ->
                        with(supportActionBar!!) {
                            if (hasActiveVisit) showStartVisitImpossibleDialog(title)
                            else startVitalsMeasurement()
                        }
                    }
            }
        }
    }

    fun endActiveVisit(){
        viewModel.endActiveVisit().observe(this) { visitEnded ->
            if (visitEnded) {
                startVitalsMeasurement()
            }
        }
    }

    private fun startVitalsMeasurement(){
        Intent(this, VitalsFormActivity::class.java).apply {
            putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, viewModel.patientId.toLong())
            startActivity(this)
        }
    }

    private fun setUpStartVisitFAB() {
        binding.actionsFab.startVisitFab.isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.patient_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSynchronize -> syncPatient()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
