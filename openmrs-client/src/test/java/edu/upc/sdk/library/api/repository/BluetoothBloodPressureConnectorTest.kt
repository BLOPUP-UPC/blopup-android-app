package edu.upc.sdk.library.api.repository

import com.ideabus.model.data.CurrentAndMData
import com.ideabus.model.data.DRecord
import com.ideabus.model.protocol.BPMProtocol
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BluetoothBloodPressureConnectorTest(private val case: TestCase) {

    private lateinit var subjectUnderTest: BluetoothBloodPressureConnector

    private lateinit var bpmProtocolFactory: BpmProtocolFactory
    private lateinit var bpmProtocol: BPMProtocol

    private lateinit var updateConnectionState: (ConnectionViewState) -> Unit
    private lateinit var updateMeasurementState: (BloodPressureViewState) -> Unit

    @Before
    fun setUp() {
        bpmProtocolFactory = mockk(relaxed = true)
        bpmProtocol = mockk(relaxed = true)
        updateConnectionState = mockk(relaxed = true)
        updateMeasurementState = mockk(relaxed = true)

        every {
            bpmProtocolFactory.getBpmProtocol()
        } returns bpmProtocol

        subjectUnderTest = BluetoothBloodPressureConnector(
            bpmProtocolFactory
        )
        subjectUnderTest.connect(
            updateConnectionState,
            updateMeasurementState
        )
    }

    @Test
    fun `given it's not connecting when device is 3G Model then the connection starts`() {
        val mac = ""
        val name = "A7 Touch BT"
        val rssi = 1

        subjectUnderTest.onScanResult(mac, name, rssi)

        verify { bpmProtocol.connect(mac) }
        verify { updateConnectionState(ConnectionViewState.Pairing) }
    }

    @Test
    fun `given connection is disconnected then isDisconnectedCallback is called`() {
        subjectUnderTest.onConnectionState(case.state)

        verify { updateConnectionState(ConnectionViewState.Disconnected) }
    }

    @Test
    fun `given bluetooth data is received then connection is stopped and measurement is updated`() {
        val dRecord = DRecord().apply {
            MData = listOf(CurrentAndMData().apply {
                systole = 107
                dia = 60
                hr = 70
            })
        }

        subjectUnderTest.onResponseReadHistory(dRecord)

        verify { bpmProtocol.stopScan() }
        verify { updateConnectionState(ConnectionViewState.Disconnected) }
        verify {
            updateMeasurementState(
                BloodPressureViewState.Content(
                    Measurement(
                        dRecord.mData[0].systole,
                        dRecord.mData[0].dia,
                        dRecord.mData[0].hr
                    )
                )
            )
        }
    }

    @Test
    fun `given the bluetooth device is connected then we ask for last data`() {
        subjectUnderTest.onResponseReadUserAndVersionData(null, null)

        verify { bpmProtocol.readHistorysOrCurrDataAndSyncTiming() }
    }

    @Test
    fun `given the bluetooth connection fails at start then we manage the error`() {
        every { bpmProtocol.startScan(any()) }.throws(IllegalArgumentException())

        subjectUnderTest.connect(
            updateConnectionState,
            updateMeasurementState
        )

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnStartScan)) }
    }

    @Test
    fun `given the bluetooth connection fails at disconnect then we manage the error`() {
        every { bpmProtocol.stopScan() }.throws(IllegalArgumentException())

        subjectUnderTest.disconnect()

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnDisconnect)) }
    }

    @Test
    fun `given the bluetooth connection fails at receiving the scan results then we manage the error`() {
        every { bpmProtocol.stopScan() }.throws(IllegalArgumentException())

        subjectUnderTest.onScanResult(null, null, 0)

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnScanResult)) }
    }

    @Test
    fun `given the bluetooth connection fails at changing the state then we manage the error`() {
        every { bpmProtocol.startScan(any()) }.throws(IllegalArgumentException())

        subjectUnderTest.onConnectionState(BPMProtocol.ConnectState.Disconnect)

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnStartScan)) }
    }

    @Test
    fun `given the bluetooth connection fails at reading the history then we manage the error`() {
        subjectUnderTest.onResponseReadHistory(null)

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnResponseReadHistory)) }
    }

    @Test
    fun `given the bluetooth connection fails at reading response then we manage the error`() {
        every { bpmProtocol.readHistorysOrCurrDataAndSyncTiming() }.throws(
            IllegalArgumentException()
        )

        subjectUnderTest.onResponseReadUserAndVersionData(null, null)

        verify { updateMeasurementState(BloodPressureViewState.Error(BluetoothConnectionException.OnResponseReadUserAndVersionData)) }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): List<TestCase> = listOf(
            TestCase(BPMProtocol.ConnectState.ConnectTimeout),
            TestCase(BPMProtocol.ConnectState.Disconnect),
            TestCase(BPMProtocol.ConnectState.ScanFinish)
        )
    }

    data class TestCase(
        val state: BPMProtocol.ConnectState
    )

}