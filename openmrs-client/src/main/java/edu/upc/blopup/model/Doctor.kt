package edu.upc.blopup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(val uuid: String, val name: String, var registrationNumber: String = "") :
    Parcelable {
    override fun toString(): String {
        return "$name - Nº Coleg. $registrationNumber"
    }
}
