package edu.upc.blopup.tensiometer.bluetooth

interface BpmProtocolFacade {

    val listener: BpmProtocolListener

    fun isConnected(): Boolean

    fun disconnect()

    fun stopScan()

    fun connect(macAddress: String?)

    fun bond(macAddress: String?)

    fun readHistorysOrCurrDataAndSyncTiming()

    fun startScan(milis: Int)

}