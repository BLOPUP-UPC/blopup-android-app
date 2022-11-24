package edu.upc.blopup.scale.bluetooth

import android.bluetooth.BluetoothDevice
import com.ideabus.model.data.EBodyMeasureData
import com.ideabus.model.protocol.EBodyProtocol

interface EBodyProtocolFacade {

    val listener: EBodyProtocolListener

    fun isConnected(): Boolean

    fun connect()

    fun disconnect()

    fun onScanResult(bluetoothDevice: BluetoothDevice)

    fun onConnectionState(state: EBodyProtocol.ConnectState?)

    fun onUserInfoUpdateSuccess()

    fun onDeleteAllUsersSuccess()

    fun onResponseMeasureResult2(data: EBodyMeasureData, impedance: Float)

    fun startScan()

    fun stopScan()

}