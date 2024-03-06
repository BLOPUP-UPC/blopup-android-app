package edu.upc.sdk.library.api.repository

import edu.upc.R

sealed class BluetoothConnectionException(val messageId: Int) : RuntimeException() {
    data object OnStartScan : BluetoothConnectionException(
        R.string.bt_exception_scan_message
    )

    data object OnDisconnect : BluetoothConnectionException(
        R.string.bt_exception_disconnect_message
    )

    data object OnScanResult : BluetoothConnectionException(
        R.string.bt_exception_connection_message
    )

    data object OnResponseReadHistory : BluetoothConnectionException(
        R.string.bt_exception_read_data_message
    )

    data object OnResponseReadUserAndVersionData : BluetoothConnectionException(
        R.string.bt_exception_pairing_message
    )
}