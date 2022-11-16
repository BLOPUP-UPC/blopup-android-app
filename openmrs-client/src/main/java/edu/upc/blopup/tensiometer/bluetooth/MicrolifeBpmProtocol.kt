package edu.upc.blopup.tensiometer.bluetooth

import com.ideabus.model.protocol.BPMProtocol
import edu.upc.blopup.tensiometer.bluetooth.BpmProtocolFacade
import edu.upc.blopup.tensiometer.bluetooth.BpmProtocolListener

class MicrolifeBpmProtocol(
    override val listener: BpmProtocolListener,
    private val bpmProtocol: BPMProtocol
) : BpmProtocolFacade {

    init {
        bpmProtocol.setOnConnectStateListener(listener)
        bpmProtocol.setOnDataResponseListener(listener)
        bpmProtocol.setOnNotifyStateListener(listener)
        bpmProtocol.setOnWriteStateListener(listener)
    }

    override fun isConnected(): Boolean = bpmProtocol.isConnected()

    override fun disconnect() = bpmProtocol.disconnect()

    override fun stopScan() = bpmProtocol.stopScan()

    override fun connect(macAddress: String?) = bpmProtocol.connect(macAddress)

    override fun bond(macAddress: String?) = bpmProtocol.bond(macAddress)

    override fun readHistorysOrCurrDataAndSyncTiming() =
        bpmProtocol.readHistorysOrCurrDataAndSyncTiming()

    override fun startScan(milis: Int) = bpmProtocol.startScan(milis)
}