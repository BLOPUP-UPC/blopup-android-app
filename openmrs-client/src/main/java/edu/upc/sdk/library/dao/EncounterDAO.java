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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.databases.entities.EncounterEntity;
import edu.upc.sdk.library.databases.entities.ObservationEntity;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Observation;
import rx.Observable;

/**
 * The type Encounter dao.
 */
@Singleton
public class EncounterDAO {

    private final EncounterRoomDAO encounterRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).encounterRoomDAO();
    private final ObservationRoomDAO observationRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).observationRoomDAO();
    private final EncounterTypeRoomDAO encounterTypeRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).encounterTypeRoomDAO();

    @Inject
    public EncounterDAO() {
    }

    /**
     * Save encounter long.
     *
     * @param encounter the encounter
     * @param visitID   the visit id
     * @return the long
     */
    public long saveEncounter(Encounter encounter, Long visitID) {
        EncounterEntity encounterEntity = AppDatabaseHelper.convert(encounter, visitID);
        return encounterRoomDAO.addEncounter(encounterEntity);
    }

    /**
     * Save last vitals encounter.
     *
     * @param encounter   the encounter
     * @param patientUUID the patient uuid
     */
    public void saveLastVitalsEncounter(Encounter encounter, String patientUUID) {
        if (null != encounter) {
            encounter.setPatientUUID(patientUUID);
            long oldLastVitalsEncounterID;
            try {
                oldLastVitalsEncounterID = encounterRoomDAO.getLastVitalsEncounterID(patientUUID).blockingGet();
            } catch (Exception e) {
                oldLastVitalsEncounterID = 0;
            }
            if (0 != oldLastVitalsEncounterID) {
                for (Observation obs : new ObservationDAO().findObservationByEncounterID(oldLastVitalsEncounterID)) {
                    ObservationEntity observationEntity = AppDatabaseHelper.convert(obs, 1L);
                    observationRoomDAO.deleteObservation(observationEntity);
                }
                encounterRoomDAO.deleteEncounterByID(oldLastVitalsEncounterID);
            }
            long encounterID = saveEncounter(encounter, null);
            for (Observation obs : encounter.getObservations()) {
                ObservationEntity observationEntity = AppDatabaseHelper.convert(obs, encounterID);
                observationRoomDAO.addObservation(observationEntity);
            }
        }
    }

    /**
     * Update encounter int.
     *
     * @param encounterID the encounter id
     * @param encounter   the encounter
     * @param visitID     the visit id
     * @return the int
     */
    public int updateEncounter(long encounterID, Encounter encounter, long visitID) {
        EncounterEntity encounterEntity = AppDatabaseHelper.convert(encounter, visitID);
        encounterEntity.setId(encounterID);
        int id = encounterRoomDAO.updateEncounter(encounterEntity);
        return id;
    }

    /**
     * Find encounters by visit id list.
     *
     * @param visitID the visit id
     * @return the list
     */
    public List<Encounter> findEncountersByVisitID(Long visitID) {
        List<Encounter> encounters = new ArrayList<>();
        try {
            List<EncounterEntity> encounterEntities = encounterRoomDAO.findEncountersByVisitID(visitID.toString()).blockingGet();

            for (EncounterEntity entity : encounterEntities) {
                encounters.add(AppDatabaseHelper.convert(entity));
            }
            return encounters;
        } catch (Exception e) {
            return encounters;
        }
    }

    /**
     * Gets all encounters by type.
     *
     * @param patientID the patient id
     * @param type      the type
     * @return the all encounters by type
     */
    public Observable<List<Encounter>> getAllEncountersByType(Long patientID, EncounterType type) {
        return AppDatabaseHelper.createObservableIO(() -> {
            List<Encounter> encounters = new ArrayList<>();
            List<EncounterEntity> encounterEntities;
            try {
                encounterEntities = encounterRoomDAO.getAllEncountersByType(patientID, type.getDisplay()).blockingGet();
                for (EncounterEntity entity : encounterEntities) {
                    encounters.add(AppDatabaseHelper.convert(entity));
                }
                return encounters;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }

    /**
     * Gets encounter by uuid.
     *
     * @param encounterUUID the encounter uuid
     * @return the encounter by uuid
     */
    public long getEncounterByUUID(final String encounterUUID) {
        try {
            return encounterRoomDAO.getEncounterByUUID(encounterUUID).blockingGet();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Delete encounter type by patient UUID.
     *
     * @param patientUuid the form name
     */
    public void deleteEncounterByPatientUUID(String patientUuid) {
        List<EncounterEntity> encounterList = encounterRoomDAO.getAllEncountersByPatientUUID(patientUuid).blockingGet();
        encounterRoomDAO.deleteEncounterByPatientUUID(patientUuid);
        for (EncounterEntity encounter : encounterList)
            observationRoomDAO.deleteObservationByEncounterId(encounter.getId());
    }
}
