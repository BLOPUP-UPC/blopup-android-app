/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.sdk.library.models

import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import edu.upc.sdk.library.models.typeConverters.PersonNameConverter

/**
 *
 * PatientDTO represents patient data on create/update API.
 * This object is serialized as request/response body to send the patient related over the wire.
 */
open class PatientDtoUpdate {

    @SerializedName("uuid")
    @Expose
    var uuid: String? = null
        protected set

    @SerializedName("identifiers")
    @Expose
    private var identifiers: List<PatientIdentifier> = ArrayList()
    
    //Contact Number
    @SerializedName("contactPhoneNumber")
    @Expose
    private var contactPhoneNumber: String? = null

    //Contact Names
    @TypeConverters(PersonNameConverter::class)
    @SerializedName("contactNames")
    @Expose
    private val contactNames: List<PersonName> = ArrayList()

    @SerializedName("person")
    @Expose
    var person: PersonUpdate? = null

    fun setIdentifiers(identifiers: List<PatientIdentifier>) {
        this.identifiers = identifiers
    }
}
