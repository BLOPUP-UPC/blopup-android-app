package edu.upc.sdk.library.api.repository

import android.bluetooth.BluetoothDevice
import com.ideabus.model.data.EBodyMeasureData
import com.ideabus.model.protocol.EBodyProtocol
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BluetoothScaleConnectorTest {

    private lateinit var subjectUnderTest: BluetoothScaleConnector

    private lateinit var eBodyProtocolFactory: EBodyProtocolFactory
    private lateinit var eBodyProtocol: EBodyProtocol

    private lateinit var updateMeasurementState: (ScaleViewState) -> Unit

    @Before
    fun setUp() {
        eBodyProtocolFactory = mockk(relaxed = true)
        eBodyProtocol = mockk(relaxed = true)
        updateMeasurementState = mockk(relaxed = true)

        every {
            eBodyProtocolFactory.getEBodyProtocol()
        } returns eBodyProtocol

        subjectUnderTest = BluetoothScaleConnector(
            eBodyProtocolFactory
        )
        subjectUnderTest.connect(updateMeasurementState)
    }

    @Test
    fun `given a device is connected then the eBodyProtocol connects to the device`() {
        val bluetoothDevice: BluetoothDevice = mockk(relaxed = true)

        subjectUnderTest.onScanResult(bluetoothDevice)

        verify { eBodyProtocol.connect(bluetoothDevice) }
    }

    @Test
    fun `given bluetooth data is received then the weight measurement is updated`() {
        val eBodyMeasureData = EBodyMeasureData().apply { weight = 70f }

        subjectUnderTest.onResponseMeasureResult2(eBodyMeasureData, 0F)

        verify {
            updateMeasurementState(
                ScaleViewState.Content(
                    WeightMeasurement(eBodyMeasureData.weight)
                )
            )
        }
    }

    @Test
    fun `given the bluetooth connection fails at disconnect then we manage the error`() {
        every { eBodyProtocol.stopScan() }.throws(IllegalArgumentException())

        subjectUnderTest.disconnect()

        verify { updateMeasurementState(ScaleViewState.Error(BluetoothConnectionException.OnDisconnect)) }
    }

    @Test
    fun `given the bluetooth connection fails at trying to scan the results then we manage the error`() {
        val bluetoothDevice: BluetoothDevice = mockk(relaxed = true)
        every { eBodyProtocol.connect(bluetoothDevice) }.throws(IllegalArgumentException())

        subjectUnderTest.onScanResult(bluetoothDevice)

        verify { updateMeasurementState(ScaleViewState.Error(BluetoothConnectionException.OnScanResult)) }
    }
}