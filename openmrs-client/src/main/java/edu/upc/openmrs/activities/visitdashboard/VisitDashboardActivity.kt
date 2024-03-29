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

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.ActivityVisitDashboardBinding
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID

@AndroidEntryPoint
class VisitDashboardActivity : edu.upc.openmrs.activities.ACBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityVisitDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.run {
            elevation = 0f
            setTitle(R.string.visit_dashboard_label)
        }

        val visitId: Long = intent.getLongExtra(VISIT_UUID, -1L).also {
            if (it == -1L) throw IllegalStateException("No valid visit id passed")
        }
        val isNewVitals:Boolean = intent.getBooleanExtra(IS_NEW_VITALS, false)

        // Create fragment
        var visitDashboardFragment = supportFragmentManager.findFragmentById(R.id.visitDashboardContentFrame) as VisitDashboardFragment?
        if (visitDashboardFragment == null) {
            visitDashboardFragment = VisitDashboardFragment.newInstance(visitId, isNewVitals)
        }
        if (!visitDashboardFragment.isActive) {
            addFragmentToActivity(supportFragmentManager, visitDashboardFragment, R.id.visitDashboardContentFrame)
        }
    }
}
