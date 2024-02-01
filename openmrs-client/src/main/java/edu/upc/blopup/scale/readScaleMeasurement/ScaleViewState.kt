package edu.upc.blopup.scale.readScaleMeasurement

import android.os.Parcelable
import com.ebelter.sdks.bean.scale.ScaleMeasureResult
import edu.upc.blopup.exceptions.BluetoothConnectionException
import kotlinx.parcelize.Parcelize

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