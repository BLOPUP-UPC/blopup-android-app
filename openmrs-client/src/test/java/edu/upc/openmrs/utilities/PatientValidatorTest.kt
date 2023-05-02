package edu.upc.openmrs.utilities

import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.PatientValidator
import edu.upc.openmrs.test.ACUnitTestBase
import org.junit.Assert.assertFalse
import org.junit.Test

class PatientValidatorTest : ACUnitTestBase() {

    private val countries = listOf("country1", "country2", "country3")

    @Test
    fun `validate patient missing first name`() {
        val validator = PatientValidator(
                createValidPatient().apply { name.givenName = null },
                isPatientUnidentified = false,
                countriesList = countries)

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient invalid first name`() {
        val validator = PatientValidator(
                createValidPatient().apply { name.givenName = INVALID_NAME_1 },
                isPatientUnidentified = false,
                countriesList = countries)

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient invalid middle name`() {
        val validator = PatientValidator(
                createValidPatient().apply { name.middleName = INVALID_NAME_2 },
                isPatientUnidentified = false,
                countriesList = countries
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing family name`() {
        val validator = PatientValidator(
                createValidPatient().apply { name.familyName = null },
                isPatientUnidentified = false,
                countriesList = countries
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient with invalid family name`() {
        val validator = PatientValidator(
                createValidPatient().apply { name.familyName = INVALID_NAME_3 },
                isPatientUnidentified = false,
                countriesList = countries
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing gender`() {
        val validator = PatientValidator(
                createValidPatient().apply { gender = null },
                isPatientUnidentified = false,
                countriesList = countries
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    @Test
    fun `validate patient missing birthdate`() {
        val validator = PatientValidator(
                createValidPatient().apply { birthdate = null },
                isPatientUnidentified = false,
                countriesList = countries
        )

        val isValid = validator.validate()

        assertFalse(isValid)
    }

    private fun createValidPatient() = updatePatientData(1L,
        Patient()
    )

    companion object {
        private const val INVALID_NAME_1 = "#James"
        private const val INVALID_NAME_2 = "John@Doe"
        private const val INVALID_NAME_3 = "Em*%ile"
    }
}
