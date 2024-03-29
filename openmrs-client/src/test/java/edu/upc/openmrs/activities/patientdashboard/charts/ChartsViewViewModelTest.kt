package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.Instant as JavaInstant

@RunWith(MockitoJUnitRunner::class)
class ChartsViewViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var treatmentRepository: TreatmentRepository

    private lateinit var viewModel: ChartsViewViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ChartsViewViewModel(patientDAO, treatmentRepository)
    }

    @Test
    fun `should transform treatments to adherences with dates`() {
        val now = Instant.now()
        val nowJava = JavaInstant.ofEpochMilli(now.millis)
        val today = LocalDate.ofInstant(nowJava, ZoneOffset.UTC)

        val lastWeek = today.minusDays(7)

        val treatmentsWOAdherence = TreatmentExample.activeTreatment().apply {
            adherence = emptyMap()
        }
        val treatment = TreatmentExample.activeTreatment().apply {
            adherence = mapOf(
                lastWeek to false,
                today to true
            )
        }
        val treatmentWithFalseAdherence = TreatmentExample.activeTreatment().apply {
            adherence = mapOf(
                today to false
            )
        }

        val expectedTreatmentAdherence =
            mapOf(
                lastWeek to listOf(
                    TreatmentAdherence(
                        treatment.medicationName,
                        treatment.medicationType,
                        false,
                        lastWeek
                    )
                ),
                today to listOf(
                    TreatmentAdherence(
                        treatmentWithFalseAdherence.medicationName,
                        treatmentWithFalseAdherence.medicationType,
                        false,
                        today
                    ),
                    TreatmentAdherence(
                        treatment.medicationName,
                        treatment.medicationType,
                        true,
                        today
                    ),
                )
            )
        val treatmentList = listOf(treatmentsWOAdherence, treatment, treatmentWithFalseAdherence)
        val patientId = 88L
        val testPatient = Patient().apply {
            id = patientId
            uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
        }

        every { patientDAO.findPatientByID(any()) } returns testPatient
        coEvery { treatmentRepository.fetchAllTreatments(any()) } returns Result.Success(treatmentList)

        runBlocking {
            viewModel.fetchTreatments(patientId.toInt())

            coVerify { treatmentRepository.fetchAllTreatments(testPatient) }

            Assert.assertEquals(Result.Success(expectedTreatmentAdherence), viewModel.treatments.value)
        }
    }
}