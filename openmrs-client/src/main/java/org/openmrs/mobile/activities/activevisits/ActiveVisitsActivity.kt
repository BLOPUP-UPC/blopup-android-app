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
package org.openmrs.mobile.activities.activevisits

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity

@AndroidEntryPoint
class ActiveVisitsActivity : ACBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_visits)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.elevation = 0f
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setTitle(R.string.action_active_visits)
        }
        // Create fragment
        var activeVisitsFragment = supportFragmentManager.findFragmentById(R.id.activeVisitContentFrame) as ActiveVisitsFragment?
        if (activeVisitsFragment == null) {
            activeVisitsFragment = ActiveVisitsFragment.newInstance()
        }
        if (!activeVisitsFragment.isActive) {
            addFragmentToActivity(supportFragmentManager,
                    activeVisitsFragment, R.id.activeVisitContentFrame)
        }
    }
}
