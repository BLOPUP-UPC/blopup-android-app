package edu.upc.openmrs.utilities

import edu.upc.BuildConfig
import edu.upc.Secrets

class SecretsUtils {

    companion object {
        fun getDoctorPhoneNumber() = if (BuildConfig.DEBUG) {
            Secrets().getDebugDoctorPhoneNumber("edu.upc")
        } else Secrets().getDoctorPhoneNumber("upc.edu")

    }
}