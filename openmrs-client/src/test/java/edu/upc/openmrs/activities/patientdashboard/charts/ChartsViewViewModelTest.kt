package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import edu.upc.sdk.utilities.DateUtils.toLocalDate
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@RunWith(JUnit4::class)
class ChartsViewViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var visitRepository: VisitRepository

    @MockK
    lateinit var treatmentRepository: TreatmentRepository

    @InjectMockKs
    private lateinit var viewModel: BloodPressureChartViewModel


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
    }

    @Test
    fun `should remove duplicates in visits and keep the latest`() = runTest {
        val today = parseInstantFromOpenmrsDate("2021-09-01T10:00:00.000+0000")

        val todayTwoHoursLater = today.plus(2, ChronoUnit.HOURS)
        val lastWeek = today.minus(7, ChronoUnit.DAYS)

        val lastWeekVisit = VisitExample.random(startDateTime = lastWeek)
        val todaysVisit = VisitExample.random(startDateTime = today)
        val todayRepeatedVisit = VisitExample.random(startDateTime = todayTwoHoursLater)

        val treatmentsWOAdherence = TreatmentExample.activeTreatment().apply {
            adherence = emptyMap()
        }
        val treatment = TreatmentExample.activeTreatment().apply {
            medicationName = "Tylenol"
            adherence = mapOf(
                lastWeek.toLocalDate() to false,
                today.toLocalDate() to true
            )
        }
        val treatmentWithFalseAdherence = TreatmentExample.activeTreatment().apply {
            medicationName = "Aspirin"
            adherence = mapOf(
                today.toLocalDate() to false
            )
        }

        val expectedVisitsWithAdherence = listOf(
            VisitWithAdherence(lastWeekVisit, listOf(
                TreatmentAdherence(
                    treatment.medicationName,
                    treatment.medicationType,
                    false,
                    lastWeek.toLocalDate()
                )
            )),
            VisitWithAdherence(todayRepeatedVisit, listOf(
                TreatmentAdherence(
                    treatmentWithFalseAdherence.medicationName,
                    treatmentWithFalseAdherence.medicationType,
                    false,
                    today.toLocalDate()
                ),
                TreatmentAdherence(
                    treatment.medicationName,
                    treatment.medicationType,
                    true,
                    today.toLocalDate()
                ),
            )),
        )
        val treatmentList = listOf(treatmentsWOAdherence, treatment, treatmentWithFalseAdherence)
        val patientId = 88L
        val testPatient = Patient().apply {
            id = patientId
            uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
        }

        every { patientDAO.findPatientByID(any()) } returns testPatient
        coEvery { visitRepository.getVisitsByPatientUuid(any()) } returns listOf(lastWeekVisit, todaysVisit, todayRepeatedVisit)
        coEvery { treatmentRepository.fetchAllTreatments(UUID.fromString(testPatient.uuid)) } returns Result.Success(treatmentList)

        runBlocking {
            viewModel.fetchVisitsWithTreatments(testPatient.id!!.toInt(), UUID.fromString(testPatient.uuid))

            Assert.assertEquals(ResultUiState.Success(expectedVisitsWithAdherence), viewModel.visitsWithTreatments.value)
        }
    }

    @Test
    fun `should return no info when list of adherence is empty`() {
        val adherence = emptyList<TreatmentAdherence>()

        Assert.assertEquals(FollowTreatments.NO_INFO, adherence.followTreatments())
    }

    @Test
    fun `should return some when some adherence are true`() {
        val adherence = listOf(
            TreatmentAdherence("Tylenol", setOf(), true, LocalDate.now()),
            TreatmentAdherence("Aspirin", setOf(), false, LocalDate.now())
        )

        Assert.assertEquals(FollowTreatments.FOLLOW_SOME, adherence.followTreatments())
    }
}