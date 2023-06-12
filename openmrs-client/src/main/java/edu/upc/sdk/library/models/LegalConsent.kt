package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LegalConsent: java.io.Serializable{

    @SerializedName("filePath")
    @Expose
    var filePath: String? = null

    @SerializedName("patientIdentifier")
    @Expose
    var patientIdentifier: String? = null

}
