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
package edu.upc.openmrs.activities.visit

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.ActivityVisitBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.IS_NEW_VITALS
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID

@AndroidEntryPoint
class VisitActivity : ACBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.run {
            elevation = 0f
            setTitle(R.string.visit_dashboard_label)
        }

        val visitUuid = intent.getStringExtra(VISIT_UUID).also {
            if (it == null) throw IllegalStateException("No valid visit uuid passed")
        }
        val isNewVitals:Boolean = intent.getBooleanExtra(IS_NEW_VITALS, false)

        // Create fragment
        var visitFragment = supportFragmentManager.findFragmentById(R.id.visitDashboardContentFrame) as VisitFragment?
        if (visitFragment == null) {
            visitFragment = VisitFragment.newInstance(visitUuid!!, isNewVitals)
        }
        if (!visitFragment.isActive) {
            addFragmentToActivity(supportFragmentManager, visitFragment, R.id.visitDashboardContentFrame)
        }
    }
}
