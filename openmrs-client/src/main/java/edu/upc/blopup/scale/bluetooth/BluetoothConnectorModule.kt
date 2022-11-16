package edu.upc.blopup.scale.bluetooth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object BluetoothConnectorModule {

    @Provides
    fun providesBluetoothConnector(connector: EBodyMicrolifeBluetoothConnector): BluetoothConnectorInterface {
        return connector
    }
}