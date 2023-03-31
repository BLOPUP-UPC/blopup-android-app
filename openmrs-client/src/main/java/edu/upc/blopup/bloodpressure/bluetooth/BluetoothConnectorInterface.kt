package edu.upc.blopup.bloodpressure.bluetooth

import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState

interface BluetoothConnectorInterface {
    fun connect(
        updateConnectionState: (ConnectionViewState) -> Unit,
        updateMeasurementState: (BloodPressureViewState) -> Unit
    )

    fun disconnect()
}