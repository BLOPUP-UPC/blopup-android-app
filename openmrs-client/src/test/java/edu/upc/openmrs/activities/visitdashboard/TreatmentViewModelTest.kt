package edu.upc.openmrs.activities.visitdashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class TreatmentViewModelTest {

    private lateinit var mockTreatmentRepo: TreatmentRepository
    private lateinit var treatmentViewModel: TreatmentViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockTreatmentRepo = mockk()
        treatmentViewModel = TreatmentViewModel(mockTreatmentRepo)
    }

    @Test
    fun `should register treatment`() {
        val treatment = Treatment()
        treatmentViewModel.treatment.value = treatment

        coEvery { mockTreatmentRepo.saveTreatment(treatment) } returns Unit

        runBlocking {
            treatmentViewModel.registerTreatment()
            coVerify { mockTreatmentRepo.saveTreatment(treatment) }
        }
    }

    @Test
    fun `should get all active treatments`() {
        val patient = Patient()
        val treatmentList = listOf<Treatment>()

        coEvery { mockTreatmentRepo.fetchActiveTreatments(patient) } returns treatmentList

        runBlocking {
            treatmentViewModel.fetchActiveTreatments(patient)
            coVerify { mockTreatmentRepo.fetchActiveTreatments(patient) }
            assert(treatmentViewModel.activeTreatments.value == treatmentList)
        }
    }
}