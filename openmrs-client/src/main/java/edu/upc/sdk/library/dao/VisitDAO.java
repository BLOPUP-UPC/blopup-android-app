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

package edu.upc.sdk.library.dao;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.databases.entities.DiagnosisEntity;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.databases.entities.ObservationEntity;
import edu.upc.sdk.library.databases.entities.VisitEntity;
import edu.upc.sdk.library.models.Diagnosis;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.Visit;
import rx.Observable;

/**
 * The type Visit dao.
 */
@Singleton
public class VisitDAO {

    /**
     * The Context.
     */
    Context context;
    /**
     * The Observation room dao.
     */
    ObservationRoomDAO observationRoomDAO;
    /**
     * The Visit room dao.
     */
    VisitRoomDAO visitRoomDAO;
    /**
     * The Encounter room dao.
     */
    EncounterDAO encounterDAO;
    /**
     * The Diagnosis room dao.
     */
    DiagnosisRoomDAO diagnosisRoomDAO;

    @Inject
    public VisitDAO() {
        context = OpenmrsAndroid.getInstance().getApplicationContext();
        observationRoomDAO = AppDatabase.getDatabase(context).observationRoomDAO();
        visitRoomDAO = AppDatabase.getDatabase(context).visitRoomDAO();
        diagnosisRoomDAO = AppDatabase.getDatabase(context).diagnosisRoomDAO();
        encounterDAO = new EncounterDAO();
    }

    public VisitDAO(Context context, ObservationRoomDAO observationRoomDAO, VisitRoomDAO visitRoomDAO, DiagnosisRoomDAO diagnosisRoomDAO) {
        this.context = context;
        this.observationRoomDAO = observationRoomDAO;
        this.visitRoomDAO = visitRoomDAO;
        this.diagnosisRoomDAO = diagnosisRoomDAO;
    }

    /**
     * Save or update observable.
     *
     * @param visit     the visit
     * @param patientId the patient id
     * @return the observable
     */
    public Observable<Long> saveOrUpdate(Visit visit, long patientId) {
        return AppDatabaseHelper.createObservableIO(() -> {
            Long visitId = visit.getId();
            if (visitId == null) {
                visitId = getVisitsIDByUUID(visit.getUuid()).toBlocking().first();
            }
            if (visitId > 0) {
                updateVisit(visit, visitId, patientId);
            } else {
                visitId = saveVisit(visit, patientId);
            }
            return visitId;
        });
    }

    private long saveVisit(Visit visit, long patientID) {
        EncounterDAO encounterDAO = new EncounterDAO();
        visit.setPatient(new PatientDAO().findPatientByID(String.valueOf(patientID)));
        visit.location = new LocationEntity(OpenmrsAndroid.getLocation());
        VisitEntity visitEntity = AppDatabaseHelper.convert(visit);
        long visitID = visitRoomDAO.addVisit(visitEntity);
        if (visit.getEncounters() != null) {
            for (Encounter encounter : visit.getEncounters()) {
                long encounterID = encounterDAO.saveEncounter(encounter, visitID);
                for (Observation obs : encounter.getObservations()) {
                    ObservationEntity observationEntity = AppDatabaseHelper.convert(obs, encounterID);
                    observationRoomDAO.addObservation(observationEntity);
                }
                for (Diagnosis diagnosis : encounter.getDiagnoses()) {
                    DiagnosisEntity diagnosisEntity = AppDatabaseHelper.convert(diagnosis, encounterID);
                    diagnosisRoomDAO.addDiagnosis(diagnosisEntity);
                }
            }
        }
        return visitID;
    }

    private boolean updateVisit(Visit visit, long visitID, long patientID) {
        EncounterDAO encounterDAO = new EncounterDAO();
        ObservationDAO observationDAO = new ObservationDAO();
        visit.setPatient(new PatientDAO().findPatientByID(String.valueOf(patientID)));
        if (visit.getEncounters() != null) {
            for (Encounter encounter : visit.getEncounters()) {
                long encounterID = encounterDAO.getEncounterByUUID(encounter.getUuid());

                if (encounterID > 0) {
                    encounterDAO.updateEncounter(encounterID, encounter, visitID);
                } else {
                    encounterID = encounterDAO.saveEncounter(encounter, visitID);
                }

                List<Observation> oldObs = observationDAO.findObservationByEncounterID(encounterID);
                for (Observation obs : oldObs) {
                    ObservationEntity observationEntity = AppDatabaseHelper.convert(obs, encounterID);
                    observationRoomDAO.deleteObservation(observationEntity);
                }

                for (Observation obs : encounter.getObservations()) {
                    ObservationEntity observationEntity = AppDatabaseHelper.convert(obs, encounterID);
                    observationRoomDAO.addObservation(observationEntity);
                }
            }
        }
        return visitRoomDAO.updateVisit(AppDatabaseHelper.convert(visit)) > 0;
    }

    /**
     * Gets active visits.
     *
     * @return the active visits
     */
    public Observable<List<Visit>> getActiveVisits() {
        return AppDatabaseHelper.createObservableIO(() -> {
            List<Visit> visits = new ArrayList<>();
            List<VisitEntity> visitEntities;
            try {
                visitEntities = visitRoomDAO.getActiveVisits().blockingGet();
                for (VisitEntity entity : visitEntities) {
                    visits.add(AppDatabaseHelper.convert(entity));
                }
                return visits;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }

    /**
     * Gets visits by patient id.
     *
     * @param patientID the patient id
     * @return the visits by patient id
     */
    public Observable<List<Visit>> getVisitsByPatientID(final Long patientID) {
        return AppDatabaseHelper.createObservableIO(() -> {
            List<Visit> visits = new ArrayList<>();
            List<VisitEntity> visitEntities;
            try {
                visitEntities = visitRoomDAO.getVisitsByPatientID(patientID).blockingGet();
                for (VisitEntity entity : visitEntities) {
                    visits.add(AppDatabaseHelper.convert(entity));
                }
                return visits;
            } catch (Exception e) {
                return visits;
            }
        });
    }

    /**
     * Gets active visit by patient id.
     *
     * @param patientId the patient id
     * @return the active visit by patient id
     */
    public Observable<Visit> getActiveVisitByPatientId(Long patientId) {
        return AppDatabaseHelper.createObservableIO(() -> {
            try {
                VisitEntity visitEntity = visitRoomDAO.getActiveVisitByPatientId(patientId).blockingGet();
                return AppDatabaseHelper.convert(visitEntity);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Gets visit by id.
     *
     * @param visitID the visit id
     * @return the visit by id
     */
    public Observable<Visit> getVisitByID(final Long visitID) {
        return AppDatabaseHelper.createObservableIO(() -> {
            try {
                VisitEntity visitEntity = visitRoomDAO.getVisitByID(visitID).blockingGet();
                return AppDatabaseHelper.convert(visitEntity);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Gets visits id by uuid.
     *
     * @param visitUUID the visit uuid
     * @return the visits id by uuid
     */
    public Observable<Long> getVisitsIDByUUID(final String visitUUID) {
        return AppDatabaseHelper.createObservableIO(() -> visitRoomDAO.getVisitsIDByUUID(visitUUID));
    }

    /**
     * Gets visit by uuid.
     *
     * @param uuid the uuid
     * @return the visit by uuid
     */
    public Observable<Visit> getVisitByUuid(String uuid) {
        return AppDatabaseHelper.createObservableIO(() -> {
            try {
                VisitEntity visitEntity = visitRoomDAO.getVisitByUuid(uuid).blockingGet();
                return AppDatabaseHelper.convert(visitEntity);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Delete all visits by patient.
     *
     * @param patient the patient
     * @return wether if the operation went well or not
     */
    public Observable<Boolean> deleteVisitPatient(Patient patient) {
        return AppDatabaseHelper.createObservableIO(() -> {
            visitRoomDAO.deleteVisitsByPatientId(patient.getId());
            encounterDAO.deleteEncounterByPatientUUID(patient.getUuid());
            return true;
        });
    }

    public Observable<Boolean> deleteVisitByUuid(String visitUuid) {
        return AppDatabaseHelper.createObservableIO(() -> {
            visitRoomDAO.deleteVisitByUuid(visitUuid);
            return true;
        });
    }
}
