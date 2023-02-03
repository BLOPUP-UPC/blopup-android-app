package com.openmrs.android_sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Encounter provider create
 *
 * @property providerUUID
 * @property encounterRoleUUID
 * @constructor Create empty Encounter provider create
 */
class EncounterProviderCreate(
    @field:Expose
    @field:SerializedName("provider")
    var providerUUID: String,

    @field:Expose
    @field:SerializedName("encounterRole")
    var encounterRoleUUID: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncounterProviderCreate

        if (providerUUID != other.providerUUID) return false
        if (encounterRoleUUID != other.encounterRoleUUID) return false

        return true
    }

    override fun hashCode(): Int {
        var result = providerUUID.hashCode()
        result = 31 * result + encounterRoleUUID.hashCode()
        return result
    }
}