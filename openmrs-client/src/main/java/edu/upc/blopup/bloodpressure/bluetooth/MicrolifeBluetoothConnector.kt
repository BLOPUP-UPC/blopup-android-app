package edu.upc.blopup.bloodpressure.bluetooth

import com.ideabus.model.data.CurrentAndMData
import com.ideabus.model.data.DRecord
import com.ideabus.model.data.DeviceInfo
import com.ideabus.model.data.User
import com.ideabus.model.data.VersionData
import com.ideabus.model.protocol.BPMProtocol
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.Measurement
import edu.upc.blopup.exceptions.BluetoothConnectionException
import edu.upc.blopup.hilt.CurrentActivityProvider
import javax.inject.Inject

class MicrolifeBluetoothConnector @Inject constructor(
    bpmProtocolFactory: BpmProtocolFactory
) : BluetoothConnectorInterface,
    BpmProtocolListener {

    private val bpmProtocol: BPMProtocol

    private lateinit var updateConnectionState: (ConnectionViewState) -> Unit
    private lateinit var updateMeasurementState: (BloodPressureViewState) -> Unit

    private var isConnecting = false

    init {
        bpmProtocol = bpmProtocolFactory.getBpmProtocol()
        bpmProtocol.setOnConnectStateListener(this)
        bpmProtocol.setOnDataResponseListener(this)
        bpmProtocol.setOnNotifyStateListener(this)
        bpmProtocol.setOnWriteStateListener(this)
    }

    override fun connect(
        updateConnectionState: (ConnectionViewState) -> Unit,
        updateMeasurementState: (BloodPressureViewState) -> Unit
    ) {
        this.updateConnectionState = updateConnectionState
        this.updateMeasurementState = updateMeasurementState
        this.startScan()
    }

    override fun disconnect() {
        try {
            if (bpmProtocol.isConnected) {
                bpmProtocol.disconnect()
            }
            bpmProtocol.stopScan()
            updateConnectionState(ConnectionViewState.Disconnected)
        } catch (ignore: Exception) {
            updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnDisconnect))
        }
    }

    override fun onBtStateChanged(isEnabled: Boolean) {
    }

    override fun onScanResult(mac: String?, name: String?, rssi: Int) {
        try {
            if (isConnecting) return
            isConnecting = true
            bpmProtocol.stopScan()
            //Connection
            if (name!!.startsWith("A")) {
                bpmProtocol.connect(mac)
                updateConnectionState(ConnectionViewState.Pairing)
            } else {
                bpmProtocol.bond(mac)
            }
        } catch (ignore: Exception) {
            updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnScanResult))
        }
    }

    override fun onConnectionState(state: BPMProtocol.ConnectState?) {
        when (state) {
            BPMProtocol.ConnectState.Connected -> {
                isConnecting = false
            }
            BPMProtocol.ConnectState.ConnectTimeout -> {
                isConnecting = false
                startScan()
                updateConnectionState(ConnectionViewState.Disconnected)
            }
            BPMProtocol.ConnectState.Disconnect -> {
                isConnecting = false
                startScan()
                updateConnectionState(ConnectionViewState.Disconnected)
            }
            BPMProtocol.ConnectState.ScanFinish -> {
                startScan()
                updateConnectionState(ConnectionViewState.Disconnected)
            }
            null -> TODO()
        }
    }

    override fun onResponseReadHistory(dRecord: DRecord?) {
        try {
            disconnect()
            val lastMeasurement = dRecord?.MData?.last()
            updateMeasurementState(
                BloodPressureViewState.Content(
                    Measurement.from(lastMeasurement!!)
                )
            )
        } catch (ignore: Exception) {
            updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnResponseReadHistory))
        }
    }

    override fun onResponseClearHistory(isSuccess: Boolean) {
    }

    override fun onResponseReadUserAndVersionData(user: User?, versionData: VersionData?) {
        try {
            bpmProtocol.readHistorysOrCurrDataAndSyncTiming()
        } catch (ignore: Exception) {
            updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnResponseReadUserAndVersionData))
        }
    }

    override fun onResponseWriteUser(isSuccess: Boolean) {
    }

    override fun onResponseReadLastData(
        dRecord: CurrentAndMData?,
        historyMeasuremeNumber: Int,
        userNumber: Int,
        MAMState: Int,
        isNoData: Boolean
    ) {
    }

    override fun onResponseClearLastData(isSuccess: Boolean) {
    }

    override fun onResponseReadDeviceInfo(deviceInfo: DeviceInfo?) {
    }

    override fun onResponseReadDeviceTime(deviceInfo: DeviceInfo?) {
    }

    override fun onResponseWriteDeviceTime(isSuccess: Boolean) {
    }

    override fun onNotifyMessage(message: String?) {
    }

    override fun onWriteMessage(isSuccess: Boolean, message: String?) {
    }

    private fun startScan() {
        try {
            val timeoutInSeconds = 10
            bpmProtocol.startScan(timeoutInSeconds)
        } catch (ignored: Exception) {
            updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnStartScan))
        }
    }
}

const val API_KEY = "a7kYM!jQv?4nTp19"

class BpmProtocolFactory @Inject constructor(private val activityProvider: CurrentActivityProvider) {

    private lateinit var bpmProtocol: BPMProtocol

    fun getBpmProtocol(): BPMProtocol {
        activityProvider.withActivity {
            this.runOnUiThread {
                bpmProtocol = BPMProtocol.getInstance(this, false, true, API_KEY)
            }
        }
        return bpmProtocol
    }
}