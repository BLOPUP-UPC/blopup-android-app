package edu.upc.blopup.scale.readScaleMeasurement

sealed class ConnectionViewState {
    object Disconnected : ConnectionViewState()
    object Pairing : ConnectionViewState()
}