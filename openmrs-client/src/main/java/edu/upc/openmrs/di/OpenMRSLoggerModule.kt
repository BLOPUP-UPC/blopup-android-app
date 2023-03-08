package edu.upc.openmrs.di

import edu.upc.sdk.library.OpenMRSLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LoggerModule {

    @Provides
    fun provideLogger() = OpenMRSLogger()
}
