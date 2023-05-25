package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RecordingRequest(

    @SerializedName("fileName")
    @Expose
    val name: String,

    @SerializedName("content")
    @Expose
    val content: ByteArray
)