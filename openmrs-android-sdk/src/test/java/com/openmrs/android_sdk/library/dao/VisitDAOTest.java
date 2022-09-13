package com.openmrs.android_sdk.library.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openmrs.android_sdk.library.databases.AppDatabase;
import com.openmrs.android_sdk.library.databases.AppDatabaseHelper;
import com.openmrs.android_sdk.library.databases.entities.DiagnosisEntity;
import com.openmrs.android_sdk.library.databases.entities.VisitEntity;
import com.openmrs.android_sdk.library.models.Diagnosis;
import com.openmrs.android_sdk.library.models.Encounter;
import com.openmrs.android_sdk.library.models.Observation;
import com.openmrs.android_sdk.library.models.Patient;
import com.openmrs.android_sdk.library.models.Visit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EncounterDAO.class, VisitDAO.class, AppDatabaseHelper.class})
public class VisitDAOTest {
    @Mock
    VisitRoomDAO visitRoomDAO;

    @Mock
    ObservationRoomDAO observationRoomDAO;

    @Mock
    DiagnosisRoomDAO diagnosisRoomDAO;

    @Test
    public void saveOrUpdate() throws Exception {
        // given
        String uuid = "12333323";
        long patientId = 54L;
        Visit visit = new Visit();
        visit.setUuid(uuid);
        long encounterId = 1L;
        Patient patient = new Patient();
        patient.setId(1L);
        Encounter encounter = new Encounter();

        encounter.setObservations(Arrays.asList(new Observation(), new Observation()));
        encounter.setDiagnoses(Arrays.asList(new Diagnosis()));
        visit.setEncounters(Arrays.asList(encounter));

        VisitEntity visitEntity = new VisitEntity();

        PowerMockito.stub(PowerMockito.method(AppDatabaseHelper.class, "convert", Visit.class)).toReturn(visitEntity);

        when(visitRoomDAO.getVisitsIDByUUID(uuid)).thenReturn(0L);
        when(visitRoomDAO.addVisit(visitEntity)).thenReturn(1L);

        EncounterDAO encounterDaoMock = PowerMockito.mock(EncounterDAO.class);
        PowerMockito.when(encounterDaoMock.saveEncounter(encounter, 1L)).thenReturn(encounterId);
        PowerMockito.whenNew(EncounterDAO.class)
                .withNoArguments()
                .thenReturn(encounterDaoMock);

        PatientDAO patientDaoMock = PowerMockito.mock(PatientDAO.class);
        PowerMockito.when(patientDaoMock.findPatientByID(any())).thenReturn(patient);
        PowerMockito.whenNew(PatientDAO.class)
                .withNoArguments()
                .thenReturn(patientDaoMock);

        VisitDAO visitDAO = new VisitDAO(null, observationRoomDAO, visitRoomDAO, diagnosisRoomDAO);

        // when
        visitDAO.saveOrUpdate(visit, patientId).test().awaitTerminalEvent()
                .assertCompleted()
                .assertNoErrors();

        //then
        verify(visitRoomDAO, times(1)).addVisit(visitEntity);
        verify(diagnosisRoomDAO, times(1)).addDiagnosis(any());
        verify(observationRoomDAO, times(2)).addObservation(any());

    }

    @Test
    public void saveOrUpdateShouldSaveDiagnosisContainedInTheVisit() throws Exception {
        // given
        String uuid = "12333323";
        long patientId = 54L;
        Visit visit = new Visit();
        visit.setUuid(uuid);
        long encounterId = 1L;
        Patient patient = new Patient();
        patient.setId(1L);
        Encounter encounter = new Encounter();

        encounter.setDiagnoses(Arrays.asList(new Diagnosis()));
        visit.setEncounters(Arrays.asList(encounter));

        VisitEntity visitEntity = new VisitEntity();
        DiagnosisEntity diagnosisEntity = new DiagnosisEntity();

        PowerMockito.stub(PowerMockito.method(AppDatabaseHelper.class, "convert", Visit.class)).toReturn(visitEntity);
        PowerMockito.stub(PowerMockito.method(AppDatabaseHelper.class, "convert", Diagnosis.class, Long.TYPE)).toReturn(diagnosisEntity);

        when(visitRoomDAO.getVisitsIDByUUID(uuid)).thenReturn(0L);
        when(visitRoomDAO.addVisit(visitEntity)).thenReturn(1L);

        EncounterDAO encounterDaoMock = PowerMockito.mock(EncounterDAO.class);
        PowerMockito.when(encounterDaoMock.saveEncounter(encounter, 1L)).thenReturn(encounterId);
        PowerMockito.whenNew(EncounterDAO.class)
                .withNoArguments()
                .thenReturn(encounterDaoMock);

        PatientDAO patientDaoMock = PowerMockito.mock(PatientDAO.class);
        PowerMockito.when(patientDaoMock.findPatientByID(any())).thenReturn(patient);
        PowerMockito.whenNew(PatientDAO.class)
                .withNoArguments()
                .thenReturn(patientDaoMock);

        VisitDAO visitDAO = new VisitDAO(null, observationRoomDAO, visitRoomDAO, diagnosisRoomDAO);

        // when
        visitDAO.saveOrUpdate(visit, patientId).test().awaitTerminalEvent()
                .assertCompleted()
                .assertNoErrors();

        //then
        verify(diagnosisRoomDAO, times(1)).addDiagnosis(diagnosisEntity);
    }
}