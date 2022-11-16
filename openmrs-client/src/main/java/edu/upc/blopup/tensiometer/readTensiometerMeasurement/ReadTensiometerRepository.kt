package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import edu.upc.blopup.tensiometer.bluetooth.BluetoothConnectorInterface
import javax.inject.Inject

class ReadTensiometerRepository @Inject constructor(
    private val connector: BluetoothConnectorInterface
) {

    fun start(
        updateStateCallback: (ConnectionViewState) -> Unit,
        updateMeasurementStateCallback: (TensiometerViewState) -> Unit
    ) {
        connector.connect(
            { state: ConnectionViewState -> updateStateCallback(state) },
            { state: TensiometerViewState -> updateMeasurementStateCallback(state) }
        )
    }

    fun disconnect() {
        connector.disconnect()
    }
}