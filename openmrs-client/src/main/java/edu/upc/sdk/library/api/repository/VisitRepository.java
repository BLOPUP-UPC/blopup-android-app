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

package edu.upc.sdk.library.api.repository;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.dao.EncounterDAO;
import edu.upc.sdk.library.dao.LocationDAO;
import edu.upc.sdk.library.dao.VisitDAO;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Visit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;


/**
 * The type Visit repository.
 */
@Singleton
public class VisitRepository extends BaseRepository {

    private final VisitDAO visitDAO;
    private final EncounterDAO encounterDAO;

    @Inject
    EncounterRepository encounterRepository;

    /**
     * Instantiates a new Visit repository.
     */
    @Inject
    public VisitRepository() {
        super(null);
        visitDAO = new VisitDAO();
        encounterDAO = new EncounterDAO();
    }

    /**
     * used in Unit tests with mockUp objects
     *
     * @param restApi
     * @param visitDAO
     * @param locationDAO
     * @param encounterDAO
     * @param encounterRepository
     */
    public VisitRepository(OpenMRSLogger logger, RestApi restApi, VisitDAO visitDAO, LocationDAO locationDAO, EncounterDAO encounterDAO, EncounterRepository encounterRepository) {
        super(restApi, logger);
        this.visitDAO = visitDAO;
        this.encounterDAO = encounterDAO;
        this.encounterRepository = encounterRepository;
    }

    /**
     * This method downloads visits data asynchronously from the server.
     *
     * @param patient
     */
    public Observable<List<Visit>> syncVisitsData(@NonNull final Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
            try {
                List<Visit> visits = getAllVisitsForPatient(patient).toBlocking().first();
                visitDAO.deleteVisitPatient(patient).toBlocking().subscribe();
                for (Visit visit : visits) {
                    visitDAO.saveOrUpdate(visit, patient.getId()).toBlocking().subscribe();
                }
                return visits;
            } catch (IOException e) {
                throw new Exception("Error with fetching visits by patient uuid: " + e.getMessage());
            }
        });
    }

    public Observable<List<Visit>> getAllVisitsForPatient(@NonNull Patient patient) throws IOException {
        Call<Results<Visit>> call = restApi.findVisitsByPatientUUID(patient.getUuid(), "custom:(uuid,location:ref,visitType:ref,startDatetime,stopDatetime,encounters:full)");
        Response<Results<Visit>> response = call.execute();
        if (response.isSuccessful()) {
            return Observable.just(response.body().getResults());
        } else {
            throw new IOException("Error with fetching visits by patient uuid: " + response.message());
        }
    }

    /**
     * This method is used to sync Vitals of a patient in a visit
     *
     * @param patientUuid Patient UUID to get vitals from
     * @return Encounter observable containing last vitals
     */
    public Observable<Encounter> syncLastVitals(final String patientUuid) {
        return AppDatabaseHelper.createObservableIO(() -> {
            Call<Results<Encounter>> call = restApi.getLastVitals(patientUuid, NewVisitRepository.EncounterTypes.VITALS, "full", 1, "desc");
            Response<Results<Encounter>> response = call.execute();

            if (response.isSuccessful()) {
                if (!response.body().getResults().isEmpty()) {
                    Encounter encounter = response.body().getResults().get(0);
                    encounterDAO.saveLastVitalsEncounter(encounter, patientUuid);
                    return encounter;
                }
                return new Encounter();
            } else {
                throw new IOException("Error with fetching last vitals: " + response.message());
            }
        });
    }

    public Optional<Visit> getLatestVisitWithHeight(long patientId) {
        List<Visit> visits = visitDAO.getVisitsByPatientID(patientId)
                .toBlocking().first().stream()
                .filter(visit -> visit.encounters != null &&
                        visit.encounters.stream()
                                .anyMatch(encounter -> encounter.getObservations().stream()
                                        .anyMatch(observation -> observation.getDisplay().contains("Height"))))
                .sorted(Comparator.comparing(Visit::getStartDatetime, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        if (visits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(visits.get(0));
    }

    public void deleteVisitByUuid(String visitUuid) throws IOException {
        Response<ResponseBody> response = restApi.deleteVisit(visitUuid).execute();

        if (response.isSuccessful()) {
            visitDAO.deleteVisitByUuid(visitUuid).toBlocking().first();

        } else {
            throw new IOException(response.message());
        }
    }

    public Visit getVisitByUuid(String visitUuid) throws IOException {
        try {
            Response<Visit> response = restApi.getVisitByUuid(visitUuid).execute();
            if (response != null) {
                return response.body();
            } else {
                throw new IOException("Error fetching visit by uuid: " + visitUuid);
            }
        } catch (Exception e) {
            logger.e(Objects.requireNonNull(e.getMessage()));
            throw new IOException("Error fetching visit by uuid: " + visitUuid + " " + e.getMessage());
        }
    }
}
