package edu.upc.blopup.scale.bluetooth

import android.bluetooth.BluetoothDevice
import com.ideabus.model.data.EBodyMeasureData
import edu.upc.blopup.exceptions.BluetoothConnectionException
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
import edu.upc.blopup.scale.readScaleMeasurement.WeightMeasurement
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EBodyMicrolifeBluetoothConnectorTest {

    private lateinit var subjectUnderTest: EBodyMicrolifeBluetoothConnector

    private lateinit var eBodyProtocolFactory: EBodyProtocolFactory
    private lateinit var eBodyProtocolFacade: EBodyProtocolFacade

    private lateinit var updateMeasurementState: (ScaleViewState) -> Unit

    @Before
    fun setUp() {
        eBodyProtocolFactory = mockk(relaxed = true)
        eBodyProtocolFacade = mockk(relaxed = true)
        updateMeasurementState = mockk(relaxed = true)

        every {
            eBodyProtocolFactory.getEBodyProtocol(any())
        } returns eBodyProtocolFacade

        subjectUnderTest = EBodyMicrolifeBluetoothConnector(
            eBodyProtocolFactory
        )
        subjectUnderTest.connect(updateMeasurementState)
    }

    @Test
    fun `given a device is connected then the eBodyProtocol connects to the device`() {
        val bluetoothDevice: BluetoothDevice = mockk(relaxed = true)

        subjectUnderTest.onScanResult(bluetoothDevice)

        verify { eBodyProtocolFacade.connect() }
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
        every { eBodyProtocolFacade.stopScan() }.throws(IllegalArgumentException())

        subjectUnderTest.disconnect()

        verify { updateMeasurementState(ScaleViewState.Error(BluetoothConnectionException.OnDisconnect)) }
    }

    @Test
    fun `given the bluetooth connection fails at trying to scan the results then we manage the error`() {
        val bluetoothDevice: BluetoothDevice = mockk(relaxed = true)
        every { eBodyProtocolFacade.connect() }.throws(IllegalArgumentException())

        subjectUnderTest.onScanResult(bluetoothDevice)

        verify { updateMeasurementState(ScaleViewState.Error(BluetoothConnectionException.OnScanResult)) }
    }
}