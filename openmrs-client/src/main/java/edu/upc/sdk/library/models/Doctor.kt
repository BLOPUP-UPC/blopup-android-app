package edu.upc.sdk.library.models

data class Doctor(val uuid: String, val name: String, var registrationNumber: String = "") {
    override fun toString(): String {
        return "$name - Nº Coleg. $registrationNumber"
    }
}
