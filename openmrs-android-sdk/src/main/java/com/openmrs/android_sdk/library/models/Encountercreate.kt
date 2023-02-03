/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package com.openmrs.android_sdk.library.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.openmrs.android_sdk.library.models.typeConverters.EncounterProviderCreateTypeConverter
import com.openmrs.android_sdk.library.models.typeConverters.ObservationListConverter
import java.io.Serializable

/**
 * Encountercreate
 *
 * <p> EncounterCreate Entity to be stored in RoomDB </p>
 * @constructor Create empty Encountercreate
 */
@Entity(tableName = "encountercreate")
class Encountercreate : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long? = null

    @ColumnInfo(name = "visit")
    @SerializedName("visit")
    @Expose
    var visit: String? = null

    @ColumnInfo(name = "patient")
    @SerializedName("patient")
    @Expose
    var patient: String? = null

    @ColumnInfo(name = "patientid")
    var patientId: Long? = null

    @ColumnInfo(name = "encounterType")
    @SerializedName("encounterType")
    @Expose
    var encounterType: String? = null

    @ColumnInfo(name = "formname")
    var formname: String? = null

    @ColumnInfo(name = "synced")
    var synced = false

    @TypeConverters(ObservationListConverter::class)
    @ColumnInfo(name = "obs")
    @SerializedName("obs")
    @Expose
    var observations: List<Obscreate> = ArrayList()

    @SerializedName("form")
    @Expose
    var formUuid: String? = null

    @SerializedName("location")
    @Expose
    var location: String? = null

    @TypeConverters(EncounterProviderCreateTypeConverter::class)
    @SerializedName("encounterProviders")
    @Expose
    var encounterProvider: List<EncounterProviderCreate> = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Encountercreate

        if (id != other.id) return false
        if (visit != other.visit) return false
        if (patient != other.patient) return false
        if (patientId != other.patientId) return false
        if (encounterType != other.encounterType) return false
        if (formname != other.formname) return false
        if (synced != other.synced) return false
        if (observations != other.observations) return false
        if (formUuid != other.formUuid) return false
        if (location != other.location) return false
        if (encounterProvider != other.encounterProvider) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (visit?.hashCode() ?: 0)
        result = 31 * result + (patient?.hashCode() ?: 0)
        result = 31 * result + (patientId?.hashCode() ?: 0)
        result = 31 * result + (encounterType?.hashCode() ?: 0)
        result = 31 * result + (formname?.hashCode() ?: 0)
        result = 31 * result + synced.hashCode()
        result = 31 * result + observations.hashCode()
        result = 31 * result + (formUuid?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + encounterProvider.hashCode()
        return result
    }
}
