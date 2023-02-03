package edu.upc.blopup.vitalsform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.api.repository.EncounterRepository
import com.openmrs.android_sdk.library.api.repository.FormRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.databases.entities.FormResourceEntity
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.ResultType
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.openmrs.test.ACUnitTestBaseRx
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import rx.Observable


@RunWith(JUnit4::class)
class VitalsFormViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var formRepository: FormRepository

    @Mock
    lateinit var encounterRepository: EncounterRepository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    lateinit var subjectUnderTest: VitalsFormViewModel

    private val DEFAULT_PATIENT_ID: Long = 88L
    private val vital = Vital("weight", "50")
    private val testVitalForm = listOf(vital)

    private val testPatient = Patient().apply {
        id = DEFAULT_PATIENT_ID
        uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
    }

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, DEFAULT_PATIENT_ID) }

        `when`(patientDAO.findPatientByID(ArgumentMatchers.anyString())).thenReturn(testPatient)
    }

    @Test
    fun `when we pass vitals, vitals are sent`() {
        subjectUnderTest = VitalsFormViewModel(
            patientDAO,
            formRepository,
            encounterRepository,
            savedStateHandle
        )
        `when`(formRepository.fetchFormResourceByName("Vitals")).thenReturn(
            Observable.just(FormResourceEntity().apply {
                uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
            })
        )
        `when`(encounterRepository.saveEncounter(any())).thenReturn(Observable.just(ResultType.EncounterSubmissionSuccess))

        val actualResult = subjectUnderTest.submitForm(testVitalForm)

        assertEquals(ResultType.EncounterSubmissionSuccess, actualResult.value)

        verify(encounterRepository).saveEncounter(any())
    }
}

