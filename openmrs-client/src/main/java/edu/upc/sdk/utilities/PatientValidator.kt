package edu.upc.sdk.utilities

import edu.upc.BuildConfig
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.StringUtils.ILLEGAL_CHARACTERS
import edu.upc.sdk.utilities.StringUtils.validateText

/**
 * This utility class validates patient's data presence and legality for registering.
 *
 * @param patient the patient to validate
 * @param countriesList the available countries that can be picked from as the patient's country
 */
class PatientValidator(private val patient: Patient,
                       var isLegalRecordingPresent: Boolean) {

    /**
     * Validates legality and presence of the necessary data of the patient object passed in the constructor
     */
    fun validate(): Boolean = patient.run {
        /* Checks for identified or unidentified patient */
        if (gender.isNullOrBlank()) return false
        if (birthdate.isNullOrBlank()) return false

        /* Additional checks for identified patient */

        // Validate names
        with(name) {
            if (givenName.isNullOrBlank() || !validateText(givenName, ILLEGAL_CHARACTERS)) return false
            if (familyName.isNullOrBlank() || !validateText(familyName, ILLEGAL_CHARACTERS)) return false
        }

        //Validate Nationality
        if(!attributes.any { it.attributeType?.uuid.equals(BuildConfig.NATIONALITY_ATTRIBUTE_TYPE_UUID) }) return false

        //Validate Legal Consent Recording
        if (!isLegalRecordingPresent) return false

        return true
    }
}
