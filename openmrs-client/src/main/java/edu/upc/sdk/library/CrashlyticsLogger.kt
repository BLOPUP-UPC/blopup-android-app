package edu.upc.sdk.library

import retrofit2.Response

interface CrashlyticsLogger {

    fun reportUnsuccessfulResponse(response: Response<*>, message: String)

    fun reportException(exception: Exception, message: String)

}
