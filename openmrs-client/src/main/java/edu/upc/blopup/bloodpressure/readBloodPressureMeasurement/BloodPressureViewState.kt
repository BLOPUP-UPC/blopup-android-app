package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import android.os.Parcelable
import com.ideabus.model.data.CurrentAndMData
import edu.upc.blopup.exceptions.BluetoothConnectionException
import kotlinx.android.parcel.Parcelize

sealed class BloodPressureViewState {
    data class Error(val exception: BluetoothConnectionException) : BloodPressureViewState()
    data class Content(val measurement: Measurement) : BloodPressureViewState()
}

@Parcelize
data class Measurement(
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int
) : Parcelable {
    companion object {
        fun from(from: CurrentAndMData): Measurement {
            return Measurement(from.systole, from.dia, from.hr)
        }
    }
}