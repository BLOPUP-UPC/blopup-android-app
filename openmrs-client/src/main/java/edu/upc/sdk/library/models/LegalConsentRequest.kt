package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LegalConsentRequest(

    @SerializedName("patientUuid")
    @Expose
    val patientUuid: String,

    @SerializedName("fileByteString")
    @Expose
    val fileByteString: String
)