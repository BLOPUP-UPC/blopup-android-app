package edu.upc.blopup.scale.readScaleMeasurement

import edu.upc.blopup.scale.bluetooth.BluetoothConnectorInterface
import javax.inject.Inject

class ReadScaleRepository @Inject constructor(
    private val connector: BluetoothConnectorInterface
) {

    fun start(
        updateStateCallback: (ConnectionViewState) -> Unit,
        updateMeasurementStateCallback: (ScaleViewState) -> Unit
    ) {
        connector.connect(
            { state: ConnectionViewState -> updateStateCallback(state) },
            { state: ScaleViewState -> updateMeasurementStateCallback(state) }
        )
    }

    fun disconnect() {
        connector.disconnect()
    }
}