/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The type Location repository.
 */
@Singleton
class LocationRepository @Inject constructor(
    private val restApi: RestApi
) {
    /**
     * Gets location (only has uuid).
     *
     * @return the location LocationEntity
     */
    val location: LocationEntity?
        get() {
            val response = restApi.getLocations(null).execute()
            if (response.isSuccessful) {
                for (result in response.body()!!.results) {
                    if (result.display?.trim()
                            .equals(OpenmrsAndroid.getLocation().trim(), ignoreCase = true)
                    ) {
                        return result
                    }
                }
            }
            return null
        }

    fun getCurrentLocation(): Result<String> {
        return try {
            val response = OpenmrsAndroid.getLocation().trim()
            if (response.isNotEmpty()) {
                Result.Success(response)
            } else {
                Result.Error(Exception("Error fetching location"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun setLocation(location: String): Result<Boolean> {
        return try {
            OpenmrsAndroid.setLocation(location)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllLocations(): Result<List<LocationEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.getLocations(null).execute()

                if (response.isSuccessful) {
                    Result.Success(response.body()?.results ?: emptyList())
                } else {
                    Result.Error(Exception("Error fetching locations"))
                }
            } catch (e: IOException) {
                Result.Error(e)
            }
        }
    }
}
