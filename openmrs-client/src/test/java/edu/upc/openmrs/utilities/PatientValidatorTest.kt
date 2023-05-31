package edu.upc.openmrs.utilities

import edu.upc.BuildConfig
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.PatientValidator
import edu.upc.openmrs.test.ACUnitTestBase
import edu.upc.sdk.library.models.PersonAttribute
import edu.upc.sdk.library.models.PersonAttributeType
import org.junit.Assert.assertFalse
import org.junit.Test

class PatientValidatorTest : ACUnitTestBase() {

    @Test
    fun `validate patient missing first name`() {
        val validator = PatientValidator(
            createValidPatient().apply { name.givenName = null },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient invalid first name`() {
        val validator = PatientValidator(
            createValidPatient().apply { name.givenName = INVALID_NAME_1 },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient invalid middle name`() {
        val validator = PatientValidator(
            createValidPatient().apply { name.middleName = INVALID_NAME_2 },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing family name`() {
        val validator = PatientValidator(
            createValidPatient().apply { name.familyName = null },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient with invalid family name`() {
        val validator = PatientValidator(
            createValidPatient().apply { name.familyName = INVALID_NAME_3 },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing gender`() {
        val validator = PatientValidator(
            createValidPatient().apply { gender = null },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing birthdate`() {
        val validator = PatientValidator(
            createValidPatient().apply { birthdate = null },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing nationality`() {
        val validator = PatientValidator(
            createValidPatient().apply { attributes = emptyList() },
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing legal consent`() {
        val validator = PatientValidator(
            createValidPatient(),
            isPatientUnidentified = false,
            isLegalRecordingPresent = false
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `returns true when patient is valid`() {
        val validator = PatientValidator(
            createValidPatient(),
            isPatientUnidentified = false,
            isLegalRecordingPresent = true
        )

        val isValid = validator.validate()

        assert(isValid)
    }

    private fun createValidPatient() = updatePatientData(1L, newPatientWithNationality())

    private fun newPatientWithNationality() = Patient().apply {
        attributes.apply {
            listOf(PersonAttribute().apply {
                attributeType = PersonAttributeType().apply {
                    uuid = BuildConfig.NATIONALITY_ATTRIBUTE_TYPE_UUID
                }
            })
        }
    }


    companion object {
        private const val INVALID_NAME_1 = "#James"
        private const val INVALID_NAME_2 = "John@Doe"
        private const val INVALID_NAME_3 = "Em*%ile"
    }
}
