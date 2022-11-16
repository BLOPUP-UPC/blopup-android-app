package edu.upc.blopup.tensiometer.readTensiometerMeasurement

sealed class ConnectionViewState {
    object Disconnected : ConnectionViewState()
    object Pairing : ConnectionViewState()
}