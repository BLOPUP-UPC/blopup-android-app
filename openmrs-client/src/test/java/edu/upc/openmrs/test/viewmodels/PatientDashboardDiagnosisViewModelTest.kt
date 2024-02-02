package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.patientdashboard.diagnosis.PatientDashboardDiagnosisViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.dao.EncounterDAO
import edu.upc.sdk.library.models.Diagnosis
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import rx.Observable

@RunWith(JUnit4::class)
class PatientDashboardDiagnosisViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var encounterDAO: EncounterDAO

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PatientDashboardDiagnosisViewModel

    lateinit var diagnosisList: List<String>

    private lateinit var observations: MutableList<Observation>

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel = PatientDashboardDiagnosisViewModel(encounterDAO, savedStateHandle)
        diagnosisList = createDiagnosisList(2)
        observations = createObservations(diagnosisList)
    }

    @Test
    fun fetchDiagnoses_success() {
        val encounters: List<Encounter> = listOf(
                Encounter(
                        diagnoses = listOf(
                                Diagnosis(display = "Covid"),
                                Diagnosis(display = "Cough")
                        )
                ),
                Encounter(
                        diagnoses = listOf(
                                Diagnosis(display = "Headache")
                        )
                )
        )

        Mockito.`when`(encounterDAO.getAllEncountersByType(eq(PATIENT_ID.toLong()), any()))
                .thenReturn(Observable.just(encounters))

        viewModel.fetchDiagnoses()

        val actualResult = (viewModel.result.value as Result.Success).data
        assertIterableEquals(listOf("Covid", "Cough", "Headache"), actualResult)
    }

    @Test
    fun fetchDiagnoses_success_shouldNotShowDuplicates() {
        val encounters = createEncounters(observations, true)
        Mockito.`when`(encounterDAO.getAllEncountersByType(eq(PATIENT_ID.toLong()), any()))
                .thenReturn(Observable.just(encounters))

        viewModel.fetchDiagnoses()

        val actualResult = (viewModel.result.value as Result.Success).data
        assertIterableEquals(diagnosisList, actualResult)
    }


    private fun createEncounters(observations: MutableList<Observation>, withDuplicates: Boolean): List<Encounter> {
        if (withDuplicates) observations.addAll(observations)
        val encounter = Encounter()
        encounter.observations = observations
        encounter.diagnoses = diagnosisList.map { Diagnosis(it) }
        return listOf(encounter)
    }

    private fun createObservations(diagnosisList: List<String>): MutableList<Observation> {
        val observations = ArrayList<Observation>()
        for (diag in diagnosisList) {
            val observation = Observation()
            observation.diagnosisList = diag
            observations.add(observation)
        }
        return observations
    }

    private fun createDiagnosisList(diagnosisCount: Int): List<String> {
        val diagnosisList: MutableList<String> = ArrayList()
        for (i in 0 until diagnosisCount) {
            diagnosisList.add("diag$i")
        }
        return diagnosisList
    }

    companion object {
        const val PATIENT_ID = "1"
    }
}
