package edu.upc.blopup.exceptions

import org.openmrs.mobile.R

sealed class BluetoothConnectionException(val messageId: Int) : RuntimeException() {
    object OnStartScan : BluetoothConnectionException(
        R.string.bt_exception_scan_message
    )

    object OnDisconnect : BluetoothConnectionException(
        R.string.bt_exception_disconnect_message
    )

    object OnScanResult : BluetoothConnectionException(
        R.string.bt_exception_connection_message
    )

    object OnResponseReadHistory : BluetoothConnectionException(
        R.string.bt_exception_read_data_message
    )

    object OnResponseReadUserAndVersionData : BluetoothConnectionException(
        R.string.bt_exception_pairing_message
    )
}