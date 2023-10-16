package edu.upc

class Secrets {

    // Method calls will be added by gradle task hideSecret
    // Example : external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }

    external fun getDoctorPhoneNumber(packageName: String): String

    external fun getDebugDoctorPhoneNumber(packageName: String): String
}