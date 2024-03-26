package edu.upc.blopup.model

data class Doctor(val uuid: String, val name: String, var registrationNumber: String = "") {
    override fun toString(): String {
        return "$name - Nº Coleg. $registrationNumber"
    }
}
