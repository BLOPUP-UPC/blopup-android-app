package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Encounter provider
 *
 * <p> More about Providers https://rest.openmrs.org/#providers </p>
 * @constructor Create empty Encounter provider
 */
class EncounterProvider : Resource() {
    @SerializedName("provider")
    @Expose
    var provider: Provider? = null
    @SerializedName("encounterRole")
    @Expose
    var encounterRole: Resource? = null

}