package edu.upc.blopup.hilt

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ActivityProviderEntryPoint {
    val activityProvider: CurrentActivityProvider
}