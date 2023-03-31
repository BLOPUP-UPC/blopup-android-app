package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import edu.upc.blopup.bloodpressure.bluetooth.BluetoothConnectorInterface
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