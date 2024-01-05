package edu.upc.sdk.library.api.repository;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.powermock.api.mockito.PowerMockito.mock;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.dao.VisitDAO;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Visit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;

@RunWith(RobolectricTestRunner.class)
public class VisitRepositoryTest {

    @Mock
    private VisitDAO visitDAO;

    @Mock
    private RestApi restApi;

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

        assert (result.get().equals(latestVisitWithHeight));
    }

    @Test
    public void shouldDeleteVisitFromBackendAndAppDatabases() throws IOException {
        Visit visitToDelete = new Visit();
        String uuid = UUID.randomUUID().toString();
        visitToDelete.setUuid(uuid);
        Call<ResponseBody> call = mock(Call.class);

        when(restApi.deleteVisit(visitToDelete.getUuid())).thenReturn(call);
        when(visitDAO.deleteVisitByUuid(visitToDelete.getUuid())).thenReturn(Observable.just(true));
        when(call.execute()).thenReturn(Response.success(ResponseBody.create("", null)));

        visitRepository.deleteVisitByUuid(visitToDelete.getUuid());

        verify(visitDAO).deleteVisitByUuid(visitToDelete.getUuid());
        verify(restApi).deleteVisit(visitToDelete.getUuid());
        verify(call).execute();
    }

    @Test(expected = IOException.class)
    public void shouldNotDeleteVisitFromAppDatabaseIfDeletingFromBackendFails() throws IOException {
        Visit visitToDelete = new Visit();
        String uuid = UUID.randomUUID().toString();
        visitToDelete.setUuid(uuid);
        Call<ResponseBody> call = mock(Call.class);

        when(restApi.deleteVisit(visitToDelete.getUuid())).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(500, ResponseBody.create("", null)));

        visitRepository.deleteVisitByUuid(visitToDelete.getUuid());

        verify(visitDAO, times(0)).deleteVisitByUuid(visitToDelete.getUuid());
    }

    @Test(expected = IOException.class)
    public void shouldReturnErrorIfResponseToGetVisitByIfIsNotSuccessful() throws IOException {
        Visit visit = new Visit();
        visit.setId(1L);

        when(visitDAO.getVisitByID(visit.getId())).thenReturn(Observable.error(new IOException("Error fetching visit by id: " + visit.getId())));

        visitRepository.getVisitById(visit.getId());
    }
}
