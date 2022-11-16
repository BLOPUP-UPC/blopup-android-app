package edu.upc.blopup.tensiometer.bluetooth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object BluetoothConnectorModule {

    @Provides
    fun providesBluetoothConnector(connector: MicrolifeBluetoothConnector): BluetoothConnectorInterface {
        return connector
    }
}