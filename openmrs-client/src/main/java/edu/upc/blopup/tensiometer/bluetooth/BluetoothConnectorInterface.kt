package edu.upc.blopup.tensiometer.bluetooth

import edu.upc.blopup.tensiometer.readTensiometerMeasurement.ConnectionViewState
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.TensiometerViewState

interface BluetoothConnectorInterface {
    fun connect(
        updateConnectionState: (ConnectionViewState) -> Unit,
        updateMeasurementState: (TensiometerViewState) -> Unit
    )

    fun disconnect()
}