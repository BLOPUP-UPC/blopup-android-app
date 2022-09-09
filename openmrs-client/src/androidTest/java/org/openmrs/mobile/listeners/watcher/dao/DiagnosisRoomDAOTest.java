package org.openmrs.mobile.listeners.watcher.dao;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.openmrs.android_sdk.library.databases.AppDatabase;
import com.openmrs.android_sdk.library.databases.entities.DiagnosisEntity;
import com.openmrs.android_sdk.library.databases.entities.ObservationEntity;
import com.openmrs.android_sdk.library.models.Diagnosis;
import com.openmrs.android_sdk.library.models.Link;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class DiagnosisRoomDAOTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase mDatabase;
    private DiagnosisEntity expectedDiagnosisEntity = createDiagnosisEntity(100L, 2L, "189890", "cough", createLink("rel", "uri"));

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(
                        InstrumentationRegistry.getInstrumentation().getTargetContext(),
                        AppDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();
    }

    @Test
    public void getDiagnosisByEncounterId_ShouldGetRightDiagnosis() {
        mDatabase.diagnosisRoomDAO().addDiagnosis(expectedDiagnosisEntity);

        mDatabase.diagnosisRoomDAO().findDiagnosesByEncounterID(2L)
                .test()
                .assertValue(diagnosisEntities -> {
                    DiagnosisEntity actualDiagnosisEntity = diagnosisEntities.get(0);
                    return Objects.equals(actualDiagnosisEntity.getId(), 100L)
                            && Objects.equals(actualDiagnosisEntity.getEncounterId(), 2L)
                            && Objects.equals(actualDiagnosisEntity.getDisplay(), "cough")
                            && Objects.equals(actualDiagnosisEntity.getUuid(), "189890");
                });
    }


    private DiagnosisEntity createDiagnosisEntity(long id, long encounterId, String conceptUUID, String diagnosis, Link link) {
        DiagnosisEntity entity = new DiagnosisEntity();

        List<Link> links = new ArrayList<>();
        links.add(link);

        entity.setId(id);
        entity.setUuid(conceptUUID);
        entity.setDisplay(diagnosis);
        entity.setEncounterId(encounterId);
        entity.setLinks(links);

        return entity;

    }

    private Link createLink(String rel, String uri) {
        Link link = new Link();
        link.setRel(rel);
        link.setUri(uri);
        return link;
    }


}
