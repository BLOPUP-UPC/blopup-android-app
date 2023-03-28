package edu.upc.sdk.library.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import edu.upc.sdk.library.databases.entities.DiagnosisEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import edu.upc.sdk.library.databases.entities.DiagnosisEntity;
import io.reactivex.Single;

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

    @Query("SELECT * FROM diagnoses WHERE encounter_id = :encounterId")
    Single<List<DiagnosisEntity>> findDiagnosesByEncounterID(@NotNull Long encounterId);

}