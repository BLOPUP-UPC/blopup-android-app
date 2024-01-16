package edu.upc.openmrs.activities.visitdashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
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
        val treatment = TreatmentExample.activeTreatment()
        treatmentViewModel.treatment.value = treatment

        coEvery { mockTreatmentRepo.saveTreatment(treatment) } returns Unit

        runBlocking {
            treatmentViewModel.registerTreatment()
            coVerify { mockTreatmentRepo.saveTreatment(treatment) }
        }
    }

    @Test
    fun `should set error if treatment couldn't be registered`() {
        val exception = Exception("Error")
        coEvery { mockTreatmentRepo.saveTreatment(any()) } throws exception

        runBlocking {
            runCatching {
                treatmentViewModel.registerTreatment()
            }

            assertEquals(
                exception,
                treatmentViewModel.result.value?.let { (it as Result.Error).throwable }
            )
        }
    }

    @Test
    fun `should update a treatment with the changes introduced by the user`() {
        val treatmentToEdit = TreatmentExample.inactiveTreatment()
        treatmentViewModel.treatmentToEdit.value = treatmentToEdit

        val treatment = TreatmentExample.activeTreatment()
        treatmentViewModel.treatment.value = treatment

        coEvery {
            treatment.treatmentUuid?.let {
                mockTreatmentRepo.updateTreatment(
                    treatmentViewModel.treatmentToEdit.value!!, treatmentViewModel.treatment.value!!
                )
            }
        } returns kotlin.Result.success(true)

        runBlocking {
            treatmentViewModel.updateTreatment()
            coVerify {
                treatment.treatmentUuid?.let {
                    mockTreatmentRepo.updateTreatment(
                        treatmentViewModel.treatmentToEdit.value!!,
                        treatmentViewModel.treatment.value!!
                    )
                }
            }
        }
    }

    @Test
    fun `should set error if no value changed to update a treatment`() {
        val exceptionMessage = "No changes detected"

        coEvery {
                mockTreatmentRepo.updateTreatment(any(), any())
        } throws Exception(exceptionMessage)

        runBlocking {
            runCatching {
                treatmentViewModel.updateTreatment()
            }

            assertEquals(
                exceptionMessage,
                treatmentViewModel.result.value?.let { (it as Result.Error).throwable.message }
            )
        }
    }
}