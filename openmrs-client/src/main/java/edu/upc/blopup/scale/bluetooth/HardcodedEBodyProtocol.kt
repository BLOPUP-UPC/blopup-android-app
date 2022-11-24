package edu.upc.blopup.scale.bluetooth

import android.bluetooth.BluetoothDevice
import com.ideabus.model.data.EBodyMeasureData
import com.ideabus.model.protocol.EBodyProtocol

/**
 * This implementation of the protocol is meant to be used as a mean to simulate bluetooth
 * communication with the phone. You can change this code to simulate errors by throwing
 * exceptions, for example.
 */
class HardcodedEBodyProtocol(
    override val listener: EBodyProtocolListener
) : EBodyProtocolFacade {

    override fun isConnected(): Boolean = true

    override fun connect() {
        startScan()
    }

    override fun disconnect() {}

    override fun onScanResult(bluetoothDevice: BluetoothDevice) {}

    override fun onConnectionState(state: EBodyProtocol.ConnectState?) {}

    override fun onUserInfoUpdateSuccess() {}

    override fun onDeleteAllUsersSuccess() {}

    override fun onResponseMeasureResult2(data: EBodyMeasureData, impedance: Float) {
        data.setWeight(65.5f)
        listener.onResponseMeasureResult2(
            data, impedance
        )
    }

    override fun startScan() {
        onResponseMeasureResult2(EBodyMeasureData(), 50f)
    }

    override fun stopScan() {}
}