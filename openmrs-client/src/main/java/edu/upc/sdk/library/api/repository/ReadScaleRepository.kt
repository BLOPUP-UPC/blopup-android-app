package edu.upc.sdk.library.api.repository

import android.os.Parcelable
import com.ebelter.sdks.bean.scale.ScaleMeasureResult
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class ReadScaleRepository @Inject constructor(
    private val connector: BluetoothScaleConnectorInterface
) {
    fun isBluetoothAvailable(): Boolean {
        return connector.isBluetoothAvailable()
    }

    fun start(
        updateMeasurementStateCallback: (ScaleViewState) -> Unit
    ) {
        connector.connect { state: ScaleViewState -> updateMeasurementStateCallback(state) }
    }

    fun disconnect() {
        connector.disconnect()
    }
}

sealed class ScaleViewState {
    data class Error(val exception: BluetoothConnectionException) : ScaleViewState()
    data class Content(val weightMeasurement: WeightMeasurement) : ScaleViewState()
}

@Parcelize
data class WeightMeasurement(
    val weight: Float
) : Parcelable {
    companion object {
        fun from(from: ScaleMeasureResult): WeightMeasurement {
            return WeightMeasurement(from.weight)
        }
    }
}