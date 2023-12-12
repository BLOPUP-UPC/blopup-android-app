package edu.upc.openmrs.activities.visitdashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Treatment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class TreatmentViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `should register treatment`() {
        val treatment = Treatment()
        val mockTreatmentRepo = mockk<TreatmentRepository>()
        val treatmentViewModel = TreatmentViewModel(mockTreatmentRepo)
        treatmentViewModel.treatment.value = treatment

        coEvery { mockTreatmentRepo.saveTreatment(treatment) } returns Unit

        runBlocking {
            mockTreatmentRepo.saveTreatment(treatment)
            coVerify { mockTreatmentRepo.saveTreatment(treatment) }
        }
    }

}