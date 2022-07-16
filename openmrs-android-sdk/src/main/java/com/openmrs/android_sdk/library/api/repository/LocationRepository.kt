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
package com.openmrs.android_sdk.library.api.repository

import com.openmrs.android_sdk.library.OpenmrsAndroid
import com.openmrs.android_sdk.library.databases.entities.LocationEntity

/**
 * The type Location repository.
 */
class LocationRepository : BaseRepository() {
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
                    if (result.display?.trim().equals(OpenmrsAndroid.getLocation().trim(), ignoreCase = true)) {
                        return result
                    }
                }
            }
            return null
        }
}
