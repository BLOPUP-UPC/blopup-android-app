package edu.upc.sdk.library.models

import edu.upc.blopup.model.Doctor
import java.util.UUID

object DoctorExample {
    fun random(): Doctor {
        return Doctor(
            uuid = UUID.randomUUID().toString(),
            name = "Dr. House",
            registrationNumber = "12345"
        )
    }
}
