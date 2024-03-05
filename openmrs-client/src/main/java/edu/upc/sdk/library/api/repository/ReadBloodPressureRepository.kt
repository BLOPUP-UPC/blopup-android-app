package edu.upc.sdk.library.api.repository

import android.os.Parcelable
import com.ideabus.model.data.CurrentAndMData
import edu.upc.blopup.exceptions.BluetoothConnectionException
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class ReadBloodPressureRepository @Inject constructor(
    private val connector: BluetoothConnectorInterface
) {

    fun start(
        updateStateCallback: (ConnectionViewState) -> Unit,
        updateMeasurementStateCallback: (BloodPressureViewState) -> Unit
    ) {
        connector.connect(
            { state: ConnectionViewState -> updateStateCallback(state) },
            { state: BloodPressureViewState -> updateMeasurementStateCallback(state) }
        )
    }

    fun disconnect() {
        connector.disconnect()
    }
}

sealed class ConnectionViewState {
    data object Disconnected : ConnectionViewState()
    data object Pairing : ConnectionViewState()
}

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