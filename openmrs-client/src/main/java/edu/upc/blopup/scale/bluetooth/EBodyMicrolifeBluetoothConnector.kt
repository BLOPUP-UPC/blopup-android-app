package edu.upc.blopup.scale.bluetooth

import android.bluetooth.BluetoothDevice
import com.ideabus.model.XlogUtils
import com.ideabus.model.data.EBodyMeasureData
import com.ideabus.model.protocol.EBodyProtocol
import com.ideabus.model.protocol.EBodyProtocol.ConnectState
import edu.upc.blopup.exceptions.BluetoothConnectionException
import edu.upc.blopup.hilt.CurrentActivityProvider
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
import edu.upc.blopup.scale.readScaleMeasurement.WeightMeasurement
import javax.inject.Inject

class EBodyMicrolifeBluetoothConnector @Inject constructor(
    eBodyProtocolFactory: EBodyProtocolFactory
) : BluetoothConnectorInterface,
    EBodyProtocolListener {

    private val eBodyProtocol: EBodyProtocol

    private lateinit var updateMeasurementStateCallback: (ScaleViewState) -> Unit

    init {
        eBodyProtocol = eBodyProtocolFactory.getEBodyProtocol()
        eBodyProtocol.setOnDataResponseListener(this)
        eBodyProtocol.setOnConnectStateListener(this)
    }

    override fun connect(
        updateMeasurementState: (ScaleViewState) -> Unit
    ) {
        this.updateMeasurementStateCallback = updateMeasurementState
        this.startScan()
    }

    override fun disconnect() {
        try {
            eBodyProtocol.stopScan()
            if (eBodyProtocol.isConnected) {
                eBodyProtocol.disconnect()
            }
        } catch (ignore: Exception) {
            updateMeasurementStateCallback(ScaleViewState.Error(BluetoothConnectionException.OnDisconnect))
        }
    }

    override fun onScanResult(bluetoothDevice: BluetoothDevice) {
        try {
            eBodyProtocol.connect(bluetoothDevice)
        } catch (ignore: Exception) {
            updateMeasurementStateCallback(ScaleViewState.Error(BluetoothConnectionException.OnScanResult))
        }
    }

    override fun onConnectionState(state: ConnectState?) {}

    override fun onUserInfoUpdateSuccess() {}

    override fun onDeleteAllUsersSuccess() {}

    override fun onResponseMeasureResult2(data: EBodyMeasureData, impedance: Float) {
        updateMeasurementStateCallback(ScaleViewState.Content(WeightMeasurement(data.getWeight())));
    }

    private fun startScan() {
        try {
            eBodyProtocol.startScan()
        } catch (ignored: Exception) {
            updateMeasurementStateCallback(ScaleViewState.Error(BluetoothConnectionException.OnStartScan))
        }
    }
}

const val API_KEY_WEIGHT = "V8iwa5V2L!=w=+K@"

class EBodyProtocolFactory @Inject constructor(private val activityProvider: CurrentActivityProvider) {

    private lateinit var eBodyProtocol: EBodyProtocol

    fun getEBodyProtocol(): EBodyProtocol {
        activityProvider.withActivity {
            this.runOnUiThread {
                eBodyProtocol = EBodyProtocol.getInstance(this, false, false, API_KEY_WEIGHT)
            }
        }
        return eBodyProtocol
    }
}