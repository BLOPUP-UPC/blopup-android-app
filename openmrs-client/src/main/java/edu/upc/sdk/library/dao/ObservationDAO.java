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

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.databases.entities.ObservationEntity;
import edu.upc.sdk.library.models.Observation;

/**
 * The type Observation dao.
 */
public class ObservationDAO {
    /**
     * The Observation room dao.
     */
    final ObservationRoomDAO observationRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).observationRoomDAO();

    /**
     * Find observation by encounter id list.
     *
     * @param encounterID the encounter id
     * @return the list
     */
    public List<Observation> findObservationByEncounterID(Long encounterID) {
        List<Observation> observationList;
        List<ObservationEntity> observationEntityList;
        try {
            observationEntityList = observationRoomDAO.findObservationByEncounterID(encounterID).blockingGet();
            observationList = AppDatabaseHelper.convert(observationEntityList);
            return observationList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
