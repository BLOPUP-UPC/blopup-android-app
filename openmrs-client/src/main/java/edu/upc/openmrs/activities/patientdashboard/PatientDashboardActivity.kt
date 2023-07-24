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

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.ActivityPatientDashboardBinding
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.utilities.ThemeUtils.isDarkModeActivated
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.library.models.OperationType.PatientDeleting
import edu.upc.sdk.library.models.OperationType.PatientSynchronizing
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.NetworkUtils
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.android.synthetic.main.fragment_add_provider.*

@AndroidEntryPoint
class PatientDashboardActivity : edu.upc.openmrs.activities.ACBaseActivity() {
    private var _binding: ActivityPatientDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDashboardMainViewModel by viewModels()

    private lateinit var patientId: String
    var isActionFABOpen = false

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
            if (!viewModel.deleteLocalPatientIfDeletedInServer()) { // TODO to be removed when implementing card #145
                viewModel.syncPatientData()
            }
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

                        PatientDeleting -> {
                            ToastUtil.success(getString(R.string.delete_patient_successful))
                            finish()
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

                        PatientDeleting -> ToastUtil.error(getString(R.string.delete_patient_error))
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

    fun deletePatient() {
        viewModel.deletePatient()
    }

    private fun initViewPager() {
        val adapter = PatientDashboardPagerAdapter(supportFragmentManager, this, patientId)
        with(binding) {
            if (isDarkModeActivated()) tabhost.setBackgroundColor(resources.getColor(R.color.black_dark_mode))
            pager.offscreenPageLimit = adapter.count - 1
            pager.adapter = adapter
            tabhost.setupWithViewPager(pager)
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
                        binding.actionsFab.activityDashboardActionFab.isVisible = true

                        actionsFab.activityDashboardActionFab.apply {
                            if (position == 1) {
                                // Convert main & sub FABs into Add Allergy FAB
                                closeFABs(animate = false)
                                setImageResource(R.drawable.ic_add)
                            } else setImageResource(R.drawable.ic_edit_white_24dp)
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun setupActionFABs() {
        with(binding.actionsFab) {
            activityDashboardActionFab.setOnClickListener {
                if (!isActionFABOpen) openFABs()
                else closeFABs()
            }
            activityDashboardDeleteFab.setOnClickListener { showDeletePatientDialog() }
            activityDashboardUpdateFab.setOnClickListener { startPatientUpdateActivity(patientId.toLong()) }
        }
    }

    private fun setUpStartVisitFAB() {
        with(binding.actionsFab) {
            startVisitFab.setOnClickListener {
                showStartVisitDialog(title)
            }
            binding.actionsFab.startVisitFab.isVisible = true
            binding.actionsFab.activityDashboardActionFab.isVisible = false
        }
    }

    private fun openFABs() {
        animateMainFABIcon()
        with(binding.actionsFab) {
            customFabDeleteLl.makeVisible()
            customFabUpdateLl.makeVisible()
            customFabDeleteLl.animate()
                .translationY(-resources!!.getDimension(R.dimen.custom_fab_bottom_margin_55))
            customFabUpdateLl.animate()
                .translationY(-resources!!.getDimension(R.dimen.custom_fab_bottom_margin_105))
        }
        isActionFABOpen = true
    }

    private fun closeFABs(animate: Boolean = true) {
        if (animate) animateMainFABIcon()
        with(binding.actionsFab) {
            customFabDeleteLl.animate().translationY(0f)
            customFabUpdateLl.animate().translationY(0f)
            customFabDeleteLl.makeGone()
            customFabUpdateLl.makeGone()
        }
        isActionFABOpen = false
    }

    private fun animateMainFABIcon() {
        with(binding.actionsFab.activityDashboardActionFab) {
            if (!isActionFABOpen) {
                ObjectAnimator.ofFloat(this, "rotation", 0f, 180f).setDuration(500).start()
                handler.postDelayed(
                    { setImageDrawable(resources.getDrawable(R.drawable.ic_close_white_24dp)) },
                    400
                )
            } else {
                ObjectAnimator.ofFloat(this, "rotation", 180f, 0f).setDuration(500).start()
                handler.postDelayed(
                    { setImageDrawable(resources.getDrawable(R.drawable.ic_edit_white_24dp)) },
                    400
                )
            }
        }
    }

    private fun startPatientUpdateActivity(patientId: Long) {
        Intent(this, AddEditPatientActivity::class.java)
            .putExtra(PATIENT_ID_BUNDLE, patientId.toString())
            .apply { startActivity(this) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.patient_details_menu, menu)
        menuInflater.inflate(R.menu.patient_dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSynchronize -> syncPatient()
            R.id.actionDelete -> showDeletePatientDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (isActionFABOpen) closeFABs()
        else {
            super.onBackPressed()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
