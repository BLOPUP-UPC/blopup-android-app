package edu.upc.sdk.library.api.repository;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.upc.sdk.library.dao.VisitDAO;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Visit;
import rx.Observable;

@RunWith(RobolectricTestRunner.class)
public class VisitRepositoryTest {

    @Mock
    private VisitDAO visitDAO;

    @InjectMocks
    private VisitRepository visitRepository;

    private final long patientID = 1234L;

    @Before
    public void setUp() {
        openMocks(this);
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNoVisitsExist() {
        List<Visit> emptyList = new ArrayList<>();

        when(visitDAO.getVisitsByPatientID(patientID)).thenReturn(Observable.just(emptyList));

        Optional<Visit> result = visitRepository.getLatestVisitWithHeight(patientID);

        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnOnlyVisitsWithHeightData() {
        Visit mostRecentVisitWithoutHeight = new Visit();
        mostRecentVisitWithoutHeight.startDatetime = "2020-01-04";
        Visit oldVisitWithHeight = new Visit();
        oldVisitWithHeight.startDatetime = "2020-01-02";
        Encounter encounter = new Encounter();
        Observation observation = new Observation();
        observation.setDisplay("Height");
        encounter.setObservations(ImmutableList.of(observation));
        oldVisitWithHeight.setEncounters(ImmutableList.of(encounter));
        Visit latestVisitWithHeight = new Visit();
        latestVisitWithHeight.startDatetime = "2020-01-03";
        latestVisitWithHeight.setEncounters(ImmutableList.of(encounter));
        List<Visit> visits = Arrays.asList(mostRecentVisitWithoutHeight, oldVisitWithHeight, latestVisitWithHeight);

        when(visitDAO.getVisitsByPatientID(patientID)).thenReturn(Observable.just(visits));

        Optional<Visit> result = visitRepository.getLatestVisitWithHeight(patientID);

        assert(result.get().equals(latestVisitWithHeight));
    }
}
