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
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.openmrs.NavigationBar
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.library.OpenmrsAndroid
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SyncedPatientsActivity : ACBaseActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var query: String? = null
    private var addPatientMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_patients)

        setBottomNavigationBar()

        supportActionBar?.let {
            it.elevation = 0f
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.action_synced_patients)
        }

        // Create fragment
        var syncedPatientsFragment =
            supportFragmentManager.findFragmentById(R.id.syncedPatientsContentFrame) as SyncedPatientsFragment?
        if (syncedPatientsFragment == null) {
            syncedPatientsFragment = SyncedPatientsFragment.newInstance()
        }
        if (!syncedPatientsFragment.isActive) {
            addFragmentToActivity(
                supportFragmentManager,
                syncedPatientsFragment, R.id.syncedPatientsContentFrame
            )
        }
    }

    private fun setBottomNavigationBar() {
        val bottomNavigationBar = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationBar.selectedItemId = R.id.search_patients
        NavigationBar.setBottomNavigationBar(bottomNavigationBar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.find_locally_and_add_patients_menu, menu)

        enableAddPatient(OpenmrsAndroid.getSyncState())

        val searchMenuItem = menu.findItem(R.id.actionSearchLocal)
        val searchView = menu.findItem(R.id.actionSearchLocal).actionView as SearchView
        if (!query.isNullOrEmpty()) {
            searchMenuItem.expandActionView()
            searchView.setQuery(query, true)
            searchView.clearFocus()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if(query.isEmpty()) {
                    return true
                }
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    lifecycleScope.launch {
                        val syncedPatientsFragment = supportFragmentManager.findFragmentById(R.id.syncedPatientsContentFrame) as SyncedPatientsFragment?
                        syncedPatientsFragment?.fetchSyncedPatients(query)
                    }
                }, 2000)
                return true
            }
        })
        return true
    }

    private fun enableAddPatient(enabled: Boolean) {
        val resId = if (enabled) R.drawable.ic_add else R.drawable.ic_add_disabled
        addPatientMenuItem?.let {
            it.isEnabled = enabled
            it.setIcon(resId)
        }
    }
}
