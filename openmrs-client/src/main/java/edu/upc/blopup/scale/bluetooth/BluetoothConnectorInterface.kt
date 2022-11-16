package edu.upc.blopup.scale.bluetooth

import edu.upc.blopup.scale.readScaleMeasurement.ConnectionViewState
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState

interface BluetoothConnectorInterface {
    fun connect(
        updateConnectionState: (ConnectionViewState) -> Unit,
        updateMeasurementState: (ScaleViewState) -> Unit
    )

    fun disconnect()
}