package com.openmrs.android_sdk.library.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.openmrs.android_sdk.library.databases.entities.DiagnosisEntity;

/**
 * The interface Visit room dao.
 */
@Dao
public interface DiagnosisRoomDAO {

    /**
     * Add Diagnosis long.
     *
     * @param diagnosis the diagnosis entity
     * @return the long
     */
    @Insert
    long addDiagnosis(DiagnosisEntity diagnosis);

}
