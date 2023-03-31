package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

sealed class ConnectionViewState {
    object Disconnected : ConnectionViewState()
    object Pairing : ConnectionViewState()
}