/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.utilities

import edu.upc.sdk.library.models.EncounterType


object ApplicationConstants {
    const val SERVER_URL = "server_url"
    const val SESSION_TOKEN = "session_id"
    const val AUTHORIZATION_TOKEN = "authorisation"
    const val LOCATION = "location"
    const val FIRST = true
    const val VISIT_TYPE_UUID = "visit_type_uuid"
    const val LAST_SESSION_TOKEN = "last_session_id"
    const val LAST_LOGIN_SERVER_URL = "last_login_server_url"
    const val DB_NAME = "openmrs.db"
    const val DB_PASSWORD_BCRYPT_PEPPER = "$2a$08\$iUp3M1VapYpjcAXQBNX6uu"
    const val DB_PASSWORD_LITERAL_PEPPER = "Open Sesame"
    const val DEFAULT_VISIT_TYPE_UUID = "7b0f5697-27e3-40c4-8bae-f4049abfb4ed"
    const val DEFAULT_BCRYPT_ROUND = 8
    const val SPLASH_TIMER = 3500
    const val PACKAGE_NAME = "edu.upc"
    const val USER_GUIDE = "https://blopup-upc.github.io/blopup-android-user-guide/"
    const val OPENMRS_PREF_FILE = "OpenMRSPrefFile"
    const val BUNDLE = "bundle"
    const val PRIMARY_KEY_ID = "_id"
    const val ABOUT_OPENMRS_URL = "https://openmrs.org/about/"
    const val PREF_FIRST_TIME_LAUNCH = "first_time_launch"

    object OpenMRSSharedPreferenceNames {
        const val SHARED_PREFERENCES_NAME = "shared_preferences"
    }

    object API {
        const val REST_ENDPOINT = "/ws/rest/v1/"
        const val FULL = "full"
    }

    object UserKeys {
        const val USER_NAME = "username"
        const val PASSWORD = "password"
        const val HASHED_PASSWORD = "hashedPassword"
        const val USER_PERSON_NAME = "userDisplay"
        const val USER_UUID = "userUUID"
        const val LOGIN = "login"
        const val FIRST_TIME = "firstTime"
    }

    object DialogTAG {
        const val LOGOUT_DIALOG_TAG = "logoutDialog"
        const val END_VISIT_DIALOG_TAG = "endVisitDialogTag"
        const val END_VITALS_TAG = "endVitalsTag"
        const val START_VISIT_DIALOG_TAG = "startVisitDialog"
        const val START_VISIT_IMPOSSIBLE_DIALOG_TAG = "startVisitImpossibleDialog"
        const val WARNING_LOST_DATA_DIALOG_TAG = "warningLostDataDialog"
        const val SIMILAR_PATIENTS_TAG = "similarPatientsDialogTag"
        const val LOCATION_DIALOG_TAG = "locationDialogTag"
        const val CREDENTIAL_CHANGED_DIALOG_TAG = "locationDialogTag"
    }

    object RegisterPatientRequirements {
        const val MAX_PATIENT_AGE = 120
    }

    object BundleKeys {
        const val CUSTOM_DIALOG_BUNDLE = "customDialogBundle"
        const val PATIENT_ID_BUNDLE = "patientID"
        const val VISIT_UUID = "visitUUID"
        const val IS_NEW_VITALS = "isNewVitals"
        const val TREATMENT = "treatment"
    }

    object BroadcastActions {
        const val AUTHENTICATION_CHECK_BROADCAST_ACTION =
            "edu.upc.openmrs.services.AuthenticateCheckService"
    }

    object EncounterTypes {
        const val VITALS = "67a71486-1a54-468f-ac3e-7091a9a79584"

        @JvmField
        var ENCOUNTER_TYPES_DISPLAYS = arrayOf(
            EncounterType.VITALS
        )
    }

    object RequestCodes {
        const val START_SETTINGS_REQ_CODE = 102
    }

    object OpenMRSlanguage {
        const val KEY_LANGUAGE_MODE = "key_language_mode"
        val LANGUAGE_LIST = arrayOf("English", "Español", "Català")
        val LANGUAGE_CODE = arrayOf("en", "es", "ca")
    }

    object ShowCaseViewConstants {
        const val SHOW_FIND_PATIENT = 1
        const val SHOW_ACTIVE_VISITS = 2
        const val SHOW_REGISTER_PATIENT = 3
    }

    object VitalsConceptType {
        const val SYSTOLIC_FIELD_CONCEPT = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val DIASTOLIC_FIELD_CONCEPT = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val HEART_RATE_FIELD_CONCEPT = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val WEIGHT_FIELD_CONCEPT = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val HEIGHT_FIELD_CONCEPT = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    }

    object PatientDashboardTabs {
        const val DETAILS_TAB_POS = 0
        const val VISITS_TAB_POS = 1
        const val CHARTS_TAB_POS = 2
        const val TAB_COUNT = 3
    }
}
