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

package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable


/**
 * Person attribute
 *
 * <p> More on Subresources of Person https://rest.openmrs.org/#person </p>
 * @constructor Create empty Person attribute
 */
class PersonAttribute : Serializable {

    @SerializedName("attributeType")
    @Expose
    var attributeType: PersonAttributeType? = null

    @SerializedName("value")
    @Expose
    var value: String? = null

    companion object {
        const val NATIONALITY_ATTRIBUTE_UUID = "8ab9b8af-7c6c-40fb-96cf-c638f5c920b9"
    }
}
