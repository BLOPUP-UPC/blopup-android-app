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

package edu.upc.openmrs.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.ActivityDashboardBinding
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity
import edu.upc.sdk.utilities.ToastUtil


@AndroidEntryPoint
class DashboardActivity : edu.upc.openmrs.activities.ACBaseActivity() {

    private lateinit var mBinding: ActivityDashboardBinding

    private var doubleBackToExitPressedOnce: Boolean = false
    private var handler: Handler? = Handler()

    private var runnable = Runnable { doubleBackToExitPressedOnce = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home_screen-> {
                    startActivity(Intent(applicationContext, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.search_patients-> {
                    startActivity(Intent(applicationContext, SyncedPatientsActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.register_patient-> {
                    startActivity(Intent(applicationContext, AddEditPatientActivity::class.java))
                    overridePendingTransition(0, 0)
                }

            }
            true
        }

        // Create toolbar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayUseLogoEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setTitle(R.string.app_name)
        }
    }

    override fun onResume() {
        super.onResume()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.dashboard_nav_host_fragment) as NavHostFragment
        val dashboardFragment: DashboardFragment? = navHostFragment.childFragmentManager.primaryNavigationFragment as DashboardFragment?
        dashboardFragment?.bindDrawableResources()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();

            return;
        }
        this.doubleBackToExitPressedOnce = true;
        ToastUtil.notify(getString(R.string.dashboard_exit_toast_message));
        handler?.postDelayed(runnable, 2000);
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable)
    }
}
