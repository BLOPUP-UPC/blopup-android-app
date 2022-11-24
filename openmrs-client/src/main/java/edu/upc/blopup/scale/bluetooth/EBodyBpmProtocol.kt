package edu.upc.blopup.scale.bluetooth

import android.bluetooth.BluetoothDevice
import com.ideabus.model.data.EBodyMeasureData
import com.ideabus.model.protocol.EBodyProtocol


class EBodyBpmProtocol(
    override val listener: EBodyProtocolListener,
    private val eBodyProtocol: EBodyProtocol
) : EBodyProtocolFacade {

    init {
        eBodyProtocol.setOnDataResponseListener(listener)
        eBodyProtocol.setOnConnectStateListener(listener)
    }

    override fun isConnected(): Boolean = eBodyProtocol.isConnected

    override fun connect() {}

    override fun disconnect() = eBodyProtocol.disconnect()

    override fun onScanResult(bluetoothDevice: BluetoothDevice) =
        eBodyProtocol.connect(bluetoothDevice)

    override fun onConnectionState(state: EBodyProtocol.ConnectState?) {}

    override fun onUserInfoUpdateSuccess() {}

    override fun onDeleteAllUsersSuccess() {}

    override fun onResponseMeasureResult2(data: EBodyMeasureData, impedance: Float) = Unit

    override fun startScan() = eBodyProtocol.startScan()

    override fun stopScan() = eBodyProtocol.stopScan()
}