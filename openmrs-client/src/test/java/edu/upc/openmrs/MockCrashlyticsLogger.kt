package edu.upc

import retrofit2.Response

class MockCrashlyticsLogger : CrashlyticsLogger {
    override fun reportUnsuccessfulResponse(response: Response<*>, message: String) =
        print("Mocking reportUnsuccessfulResponse")


    override fun reportException(exception: Exception, message: String) =
        print("Mocking reportException")

}
