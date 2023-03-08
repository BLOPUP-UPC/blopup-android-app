package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.openmrs.activities.formentrypatientlist.FormEntryPatientListViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import rx.Observable

@RunWith(JUnit4::class)
class FormEntryPatientListViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var visitDAO: VisitDAO

    lateinit var viewModel: FormEntryPatientListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FormEntryPatientListViewModel(visitDAO)
    }

    @Test
    fun `fetch all saved patients with active visits should succeed`() {
        val visits = createVisitList()
        val patients = mutableListOf<Patient>().apply { visits.forEach { this += it.patient } }
        `when`(visitDAO.activeVisits).thenReturn(Observable.just(visits))

        viewModel.fetchSavedPatientsWithActiveVisits()
        val result = viewModel.result.value as Result.Success<List<Patient>>

        assertIterableEquals(patients, result.data)
    }

    @Test
    fun `fetch all saved patients with active visits should return error`() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)
        `when`(visitDAO.activeVisits).thenReturn(Observable.error(throwable))

        viewModel.fetchSavedPatientsWithActiveVisits()
        val result = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, result)
    }

    @Test
    fun `fetch all saved patients with active visits by query name should succeed`() {
        val visits = createVisitList()
        val patients = mutableListOf<Patient>().apply { visits.forEach { this += it.patient } }
        patients[0].name.givenName = "Alex"
        val filteredPatients = listOf(patients[0])

        `when`(visitDAO.activeVisits).thenReturn(Observable.just(visits))

        viewModel.fetchSavedPatientsWithActiveVisits("Alex")

        val result = viewModel.result.value as Result.Success<List<Patient>>
        assertIterableEquals(filteredPatients, result.data)
    }
}
