package edu.upc.openmrs.activities.visit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.Doctor
import edu.upc.sdk.library.api.repository.DoctorRepository
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
    private lateinit var mockDoctorRepo: DoctorRepository
    private lateinit var addEditTreatmentViewModel: AddEditTreatmentViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockTreatmentRepo = mockk()
        mockDoctorRepo = mockk()
        addEditTreatmentViewModel = AddEditTreatmentViewModel(mockTreatmentRepo, mockDoctorRepo)
    }

    @Test
    fun `should register treatment`() {
        val treatment = TreatmentExample.activeTreatment()
        addEditTreatmentViewModel.treatment.value = treatment

        coEvery { mockTreatmentRepo.saveTreatment(treatment) } returns Unit

        runBlocking {
            addEditTreatmentViewModel.registerTreatment()
            coVerify { mockTreatmentRepo.saveTreatment(treatment) }
        }
    }

    @Test
    fun `should set error if treatment couldn't be registered`() {
        val exception = Exception("Error")
        coEvery { mockTreatmentRepo.saveTreatment(any()) } throws exception

        runBlocking {
            runCatching {
                addEditTreatmentViewModel.registerTreatment()
            }

            assertEquals(
                exception,
                addEditTreatmentViewModel.result.value?.let { (it as Result.Error).throwable }
            )
        }
    }

    @Test
    fun `should update a treatment with the changes introduced by the user`() {
        val treatmentToEdit = TreatmentExample.inactiveTreatment()
        addEditTreatmentViewModel.treatmentToEdit.value = treatmentToEdit

        val treatment = TreatmentExample.activeTreatment()
        addEditTreatmentViewModel.treatment.value = treatment

        coEvery {
            treatment.treatmentUuid?.let {
                mockTreatmentRepo.updateTreatment(
                    addEditTreatmentViewModel.treatmentToEdit.value!!, addEditTreatmentViewModel.treatment.value!!
                )
            }
        } returns kotlin.Result.success(true)

        runBlocking {
            addEditTreatmentViewModel.updateTreatment()
            coVerify {
                treatment.treatmentUuid?.let {
                    mockTreatmentRepo.updateTreatment(
                        addEditTreatmentViewModel.treatmentToEdit.value!!,
                        addEditTreatmentViewModel.treatment.value!!
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
                addEditTreatmentViewModel.updateTreatment()
            }

            assertEquals(
                exceptionMessage,
                addEditTreatmentViewModel.result.value?.let { (it as Result.Error).throwable.message }
            )
        }
    }

    @Test
    fun `should get all doctors`() {

        val doctors = listOf(
            Doctor("providerUuid1","Xavier de las Huertas"),
            Doctor("providerUuid2","Alejandro de las Huertas"),
            Doctor("providerUuid3","Rosa de las Huertas"),
            Doctor("providerUuid4","Carolina de las Huertas"))

        coEvery { mockDoctorRepo.getAllDoctors() } returns Result.Success(doctors)

        runBlocking {
                addEditTreatmentViewModel.getAllDoctors()
        }
        assertEquals(doctors, addEditTreatmentViewModel.doctors.value)
    }
}