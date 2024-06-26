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
import edu.upc.sdk.library.databases.entities.LocationEntity
import java.io.Serializable

/**
 * Encounter
 *
 * <p> More on Encounters https://rest.openmrs.org/#encounters </p>
 * @constructor Create empty Encounter
 */
class Encounter() : Resource(), Serializable {
    override var id: Long? = null

    @SerializedName("encounterDatetime")
    @Expose
    var encounterDate: String? = null

    @SerializedName("patient")
    @Expose
    var patient: Patient? = null

    @SerializedName("location")
    @Expose
    var location: LocationEntity? = null

    @SerializedName("form")
    @Expose
    var form: Form? = null

    @SerializedName("encounterType")
    @Expose
    var encounterType: EncounterType? = null

    @SerializedName("obs")
    @Expose
    var observations: List<Observation> = ArrayList()

    @SerializedName("orders")
    @Expose
    var orders: List<Any> = ArrayList()

    @SerializedName("voided")
    @Expose
    var voided: Boolean? = null

    @SerializedName("visit")
    @Expose
    var visit: OpenMRSVisit? = null

    @SerializedName("encounterProviders")
    @Expose
    var encounterProviders: List<EncounterProvider> = ArrayList()

    @SerializedName("resourceVersion")
    @Expose
    var resourceVersion: String? = null

    var visitID: Long? = null
    var visitUuid: String? = null
    var patientUUID: String? = null


    val formUuid: String?
        get() = if (form != null)
            form!!.uuid
        else
            null

    /**
     * Set encounter datetime
     *
     * @param encounterDatetime
     */
    fun setEncounterDatetime(encounterDatetime: String) {
        this.encounterDate = encounterDatetime
    }


}
