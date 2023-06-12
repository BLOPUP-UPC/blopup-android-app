package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LegalConsentRequest(

    @SerializedName("patientIdentifier")
    @Expose
    val patientIdentifier: String,

    @SerializedName("fileByteString")
    @Expose
    val fileByteString: String
)