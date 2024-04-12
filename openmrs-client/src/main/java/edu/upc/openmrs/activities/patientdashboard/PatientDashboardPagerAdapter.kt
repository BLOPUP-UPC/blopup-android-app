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

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import edu.upc.R
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardPagerAdapter.PatientDashboardTabs.CHARTS_TAB_POS
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardPagerAdapter.PatientDashboardTabs.DETAILS_TAB_POS
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardPagerAdapter.PatientDashboardTabs.TAB_COUNT
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardPagerAdapter.PatientDashboardTabs.VISITS_TAB_POS
import edu.upc.openmrs.activities.patientdashboard.charts.PatientChartsFragment
import edu.upc.openmrs.activities.patientdashboard.details.PatientDetailsFragment
import edu.upc.openmrs.activities.patientdashboard.visits.PatientVisitsFragment

class PatientDashboardPagerAdapter(private val fm: FragmentManager,
                                   private val context: Context,
                                   private val mPatientId: Long,
                                   private val mPatientUuid: String
) : FragmentPagerAdapter(fm) {

    private val registeredFragments = SparseArray<Fragment>()

    override fun getItem(i: Int): Fragment {
        return when (i) {
            DETAILS_TAB_POS -> PatientDetailsFragment.newInstance(mPatientId.toString(), mPatientUuid)
            VISITS_TAB_POS -> PatientVisitsFragment.newInstance(mPatientId, mPatientUuid)
            CHARTS_TAB_POS -> PatientChartsFragment.newInstance(mPatientId.toString(), mPatientUuid)
            else -> throw IllegalStateException()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            DETAILS_TAB_POS -> context.getString(R.string.patient_scroll_tab_details_label)
            VISITS_TAB_POS -> context.getString(R.string.patient_scroll_tab_visits_label)
            CHARTS_TAB_POS -> context.getString(R.string.patient_scroll_tab_charts_label)
            else -> super.getPageTitle(position)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getCount(): Int = TAB_COUNT

    object PatientDashboardTabs {
        const val DETAILS_TAB_POS = 0
        const val VISITS_TAB_POS = 1
        const val CHARTS_TAB_POS = 2
        const val TAB_COUNT = 3
    }
}
