package edu.upc.sdk.library.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RestApiModule {

    @Provides
    fun providesRestApi(): RestApi {
        return RestServiceBuilder.createService()
    }
}