package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EmailRequest(

    @SerializedName("subject")
    @Expose
    val subject: String,

    @SerializedName("content")
    @Expose
    val content: String
)