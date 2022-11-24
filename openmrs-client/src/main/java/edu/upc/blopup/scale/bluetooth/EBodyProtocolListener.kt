package edu.upc.blopup.scale.bluetooth

import com.ideabus.model.protocol.EBodyProtocol

interface EBodyProtocolListener : EBodyProtocol.OnConnectStateListener,
    EBodyProtocol.OnDataResponseListener