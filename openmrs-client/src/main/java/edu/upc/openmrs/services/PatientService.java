/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.openmrs.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.api.repository.PatientRepository;
import edu.upc.sdk.library.dao.PatientDAO;
import edu.upc.sdk.library.models.Module;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.ModuleUtils;
import edu.upc.sdk.utilities.NetworkUtils;
import edu.upc.sdk.utilities.PatientAndMatchingPatients;
import edu.upc.sdk.utilities.PatientComparator;
import edu.upc.sdk.utilities.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.upc.R;
import edu.upc.openmrs.activities.matchingpatients.MatchingPatientsActivity;
import edu.upc.openmrs.utilities.PatientAndMatchesWrapper;
import retrofit2.Call;
import retrofit2.Response;

public class PatientService extends IntentService {
    public static final String PATIENT_SERVICE_TAG = "PATIENT_SERVICE";
    private boolean calculatedLocally = false;

    public PatientService() {
        super("Register Patients");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (NetworkUtils.isOnline()) {
            PatientAndMatchesWrapper patientAndMatchesWrapper = new PatientAndMatchesWrapper();
            List<Patient> patientList = new PatientDAO().getUnSyncedPatients();
            final ListIterator<Patient> it = patientList.listIterator();
            while (it.hasNext()) {
                final Patient patient = it.next();
                fetchSimilarPatients(patient, patientAndMatchesWrapper);
            }
            if (!patientAndMatchesWrapper.getMatchingPatients().isEmpty()) {
                Intent intent1 = new Intent(getApplicationContext(), MatchingPatientsActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra(ApplicationConstants.BundleKeys.CALCULATED_LOCALLY, calculatedLocally);
                intent1.putExtra(ApplicationConstants.BundleKeys.PATIENTS_AND_MATCHES, patientAndMatchesWrapper);
                startActivity(intent1);
            }
        } else {
            ToastUtil.error(getString(R.string.activity_no_internet_connection) +
                getString(R.string.activity_sync_after_connection));
        }
    }

    private void fetchSimilarPatients(final Patient patient, final PatientAndMatchesWrapper patientAndMatchesWrapper) {
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<Module>> moduleCall = restApi.getModules(ApplicationConstants.API.FULL);
        try {
            Response<Results<Module>> moduleResp = moduleCall.execute();
            if (moduleResp.isSuccessful()) {
                if (ModuleUtils.isRegistrationCore1_7orAbove(moduleResp.body().getResults())) {
                    fetchSimilarPatientsFromServer(patient, patientAndMatchesWrapper);
                } else {
                    fetchPatientsAndCalculateLocally(patient, patientAndMatchesWrapper);
                }
            } else {
                fetchPatientsAndCalculateLocally(patient, patientAndMatchesWrapper);
            }
        } catch (IOException e) {
            Log.e(PATIENT_SERVICE_TAG, e.getMessage());
        }
    }

    private void fetchPatientsAndCalculateLocally(Patient patient, PatientAndMatchesWrapper patientAndMatchesWrapper) throws IOException {
        calculatedLocally = true;
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<PatientDto>> patientCall = restApi.getPatientsDto(patient.getName().getGivenName(), ApplicationConstants.API.FULL);
        Response<Results<PatientDto>> resp = patientCall.execute();
        if (resp.isSuccessful()) {
            List<Patient> patientList = new ArrayList<>();
            for(PatientDto p : resp.body().getResults()){
                patientList.add(p.getPatient());
            }
            List<Patient> similarPatient = new PatientComparator().findSimilarPatient(patientList, patient);
            if (!similarPatient.isEmpty()) {
                patientAndMatchesWrapper.addToList(new PatientAndMatchingPatients(patient, similarPatient));
            } else {
                new PatientRepository().syncPatient(patient);
            }
        }
    }

    private void fetchSimilarPatientsFromServer(Patient patient, PatientAndMatchesWrapper patientAndMatchesWrapper) throws IOException {
        calculatedLocally = false;
        RestApi restApi = RestServiceBuilder.createService(RestApi.class);
        Call<Results<Patient>> patientCall = restApi.getSimilarPatients(patient.toMap());
        Response<Results<Patient>> patientsResp = patientCall.execute();
        if (patientsResp.isSuccessful()) {
            List<Patient> patientList = patientsResp.body().getResults();
            if (!patientList.isEmpty()) {
                patientAndMatchesWrapper.addToList(new PatientAndMatchingPatients(patient, patientList));
            } else {
                new PatientRepository().syncPatient(patient);
            }
        }
    }
}
