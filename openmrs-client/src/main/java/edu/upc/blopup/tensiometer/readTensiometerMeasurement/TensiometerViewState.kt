package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import android.os.Parcelable
import com.ideabus.model.data.CurrentAndMData
import edu.upc.blopup.exceptions.BluetoothConnectionException
import kotlinx.android.parcel.Parcelize

sealed class TensiometerViewState {
    data class Error(val exception: BluetoothConnectionException) : TensiometerViewState()
    data class Content(val measurement: Measurement) : TensiometerViewState()
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