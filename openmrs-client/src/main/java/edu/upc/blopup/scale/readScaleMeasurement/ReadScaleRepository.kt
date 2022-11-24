package edu.upc.blopup.scale.readScaleMeasurement

import edu.upc.blopup.scale.bluetooth.BluetoothConnectorInterface
import javax.inject.Inject

class ReadScaleRepository @Inject constructor(
    private val connector: BluetoothConnectorInterface
) {

    fun start(
        updateMeasurementStateCallback: (ScaleViewState) -> Unit
    ) {
        connector.connect { state: ScaleViewState -> updateMeasurementStateCallback(state) }
    }

    fun disconnect() {
        connector.disconnect()
    }
}