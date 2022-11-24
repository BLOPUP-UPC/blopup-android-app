package edu.upc.blopup.scale.bluetooth

import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState

interface BluetoothConnectorInterface {
    fun connect(
        updateMeasurementState: (ScaleViewState) -> Unit
    )

    fun disconnect()
}