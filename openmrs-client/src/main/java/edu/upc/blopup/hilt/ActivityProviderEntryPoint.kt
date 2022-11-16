package edu.upc.blopup.hilt

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import edu.upc.blopup.hilt.CurrentActivityProvider

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ActivityProviderEntryPoint {
    val activityProvider: CurrentActivityProvider
}