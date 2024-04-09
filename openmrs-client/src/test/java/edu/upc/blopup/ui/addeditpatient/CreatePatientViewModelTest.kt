package edu.upc.blopup.ui.addeditpatient

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.BuildConfig
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonAttribute
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.utilities.DateUtils
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.joda.time.format.DateTimeFormat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import rx.Observable

class CreatePatientViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @InjectMockKs
    private lateinit var viewModel: CreatePatientViewModel

    @MockK
    private lateinit var patientRepository: PatientRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
    }

    @Ignore("Coroutine error. It work when running locally")
    @Test
    fun `register patient should create new patient`() = runTest {

        val name = "John"
        val familyName = "Doe"
        val dateOfBirth = ""
        val estimatedYears = "33"
        val gender = "M"
        val countryOfBirth = "Spain"

        val approximateBirthdate =
            DateUtils.getDateTimeFromDifference(estimatedYears.toInt())
        val birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
            .print(approximateBirthdate)

        val patient = Patient().apply {
            isDeceased = false
            id = 1L
            names = listOf(PersonName().apply {
                givenName = name
                this.familyName = familyName
            })
            this.birthdate = birthdate
            birthdateEstimated = true
            this.gender = gender
            attributes = listOf(PersonAttribute().apply {
                uuid = BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID
                value = countryOfBirth
            })
        }

        coEvery {
            patientRepository.registerPatient(
                name,
                familyName,
                birthdate,
                true,
                gender,
                countryOfBirth
            )
        } returns Observable.just(patient)


        viewModel.createPatient(
            name,
            familyName,
            dateOfBirth,
            estimatedYears,
            gender,
            countryOfBirth
        )

        val result = viewModel.createPatientUiState.drop(1).first()

        assertEquals(CreatePatientResultUiState.Success(1L), result)

    }

    @Ignore("Coroutine error. It work when running locally")
    @Test
    fun `if error register patient should return an error`() = runTest {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)

        coEvery {
            patientRepository.registerPatient(any(), any(), any(), any(), any(), any())
        } returns Observable.error(
            throwable
        )


        viewModel.createPatient(
            "John",
            "Doe",
            "",
            "33",
            "M",
            "Spain")

        val result = viewModel.createPatientUiState.drop(1).first()

        assertEquals(CreatePatientResultUiState.Error, result)

    }
}