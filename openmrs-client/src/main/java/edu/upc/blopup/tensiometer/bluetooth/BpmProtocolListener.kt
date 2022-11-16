package edu.upc.blopup.tensiometer.bluetooth

import com.ideabus.model.bluetooth.MyBluetoothLE
import com.ideabus.model.protocol.BPMProtocol

interface BpmProtocolListener : BPMProtocol.OnConnectStateListener,
    BPMProtocol.OnDataResponseListener,
    BPMProtocol.OnNotifyStateListener,
    MyBluetoothLE.OnWriteStateListener