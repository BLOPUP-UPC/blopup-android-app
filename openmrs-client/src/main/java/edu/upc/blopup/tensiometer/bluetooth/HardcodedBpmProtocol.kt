package edu.upc.blopup.tensiometer.bluetooth

import com.ideabus.model.data.CurrentAndMData
import com.ideabus.model.data.DRecord
import edu.upc.blopup.tensiometer.bluetooth.BpmProtocolFacade
import edu.upc.blopup.tensiometer.bluetooth.BpmProtocolListener

/**
 * This implementation of the protocol is meant to be used as a mean to simulate bluetooth
 * communication with the phone. You can change this code to simulate errors by throwing
 * exceptions, for example.
 */
class HardcodedBpmProtocol(
    override val listener: BpmProtocolListener
) : BpmProtocolFacade {

    override fun isConnected(): Boolean = true

    override fun disconnect() {}

    override fun stopScan() {}

    override fun connect(macAddress: String?) {
        listener.onResponseReadUserAndVersionData(null, null)
    }

    override fun bond(macAddress: String?) {}

    override fun readHistorysOrCurrDataAndSyncTiming() {
        listener.onResponseReadHistory(
            DRecord().apply {
                mData = listOf(
                    CurrentAndMData().apply {
                        systole = 127
                        dia = 64
                        hr = 62
                    }
                )
            }
        )
    }

    override fun startScan(milis: Int) {
        listener.onScanResult("macAddress", "A7 Touch BT", -1)
    }
}