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

package edu.upc.openmrs.activities.matchingpatients;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

import androidx.appcompat.widget.Toolbar;

import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.ToastUtil;

import dagger.hilt.android.AndroidEntryPoint;
import edu.upc.R;
import edu.upc.databinding.ActivityMatchingPatientsBinding;
import edu.upc.openmrs.activities.ACBaseActivity;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.openmrs.utilities.PatientAndMatchesWrapper;

@AndroidEntryPoint
public class MatchingPatientsActivity extends ACBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMatchingPatientsBinding binding = ActivityMatchingPatientsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;

        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.matching_patients_toolbar_title));
            setSupportActionBar(toolbar);
        }
        // Create fragment
        MatchingPatientsFragment matchingPatientsFragment =
                (MatchingPatientsFragment) getSupportFragmentManager().findFragmentById(R.id.matchingPatientsContentFrame);
        if (matchingPatientsFragment == null) {
            PatientAndMatchesWrapper patientAndMatchesWrapper = (PatientAndMatchesWrapper) getIntent().getSerializableExtra(ApplicationConstants.BundleKeys.PATIENTS_AND_MATCHES);
            matchingPatientsFragment = MatchingPatientsFragment.Companion.newInstance(patientAndMatchesWrapper.getMatchingPatients());
        }
        if (!matchingPatientsFragment.isAdded()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    matchingPatientsFragment, R.id.matchingPatientsContentFrame);
        }

        if (getIntent().getExtras().getBoolean(ApplicationConstants.BundleKeys.CALCULATED_LOCALLY, false)) {
            showToast(getString(R.string.registration_core_info));
        }

    }

    private void showToast(String message) {
        ToastUtil.notifyLong(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OpenMRS.getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("sync", false);
        editor.apply();
    }
}
