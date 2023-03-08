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

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Obscreate
 *
 * @constructor Create empty Obscreate
 */
class Obscreate : Serializable {

    @SerializedName("person")
    @Expose
    var person: String? = null

    @SerializedName("obsDatetime")
    @Expose
    var obsDatetime: String? = null

    @SerializedName("concept")
    @Expose
    var concept: String? = null

    @SerializedName("value")
    @Expose
    var value: String? = null

    @SerializedName("encounter")
    @Expose
    var encounter: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Obscreate

        if (person != other.person) return false
        if (obsDatetime != other.obsDatetime) return false
        if (concept != other.concept) return false
        if (value != other.value) return false
        if (encounter != other.encounter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = person?.hashCode() ?: 0
        result = 31 * result + (obsDatetime?.hashCode() ?: 0)
        result = 31 * result + (concept?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (encounter?.hashCode() ?: 0)
        return result
    }

}
