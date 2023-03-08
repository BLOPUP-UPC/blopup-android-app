package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.openmrs.activities.patientdashboard.charts.PatientDashboardChartsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import rx.Observable

@RunWith(JUnit4::class)
class PatientDashboardChartsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var visitDAO: VisitDAO

    lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: PatientDashboardChartsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel = PatientDashboardChartsViewModel(visitDAO, savedStateHandle)
    }

    @Test
    fun fetchChartsData_error() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)
        Mockito.`when`(visitDAO.getVisitsByPatientID(PATIENT_ID.toLong())).thenReturn(Observable.error(throwable))


        viewModel.fetchChartsData()

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }

    companion object {
        const val PATIENT_ID = "1"
    }
}
