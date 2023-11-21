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

package edu.upc.sdk.library.api.repository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.dao.ProviderRoomDAO;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Resource;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.utilities.ApplicationConstants;
import retrofit2.Response;
import rx.Observable;

/**
 * The type Provider repository.
 */
@Singleton
public class ProviderRepository extends BaseRepository {

    ProviderRoomDAO providerRoomDao;

    /**
     * Instantiates a new Provider repository.
     */
    @Inject
    public ProviderRepository() {
        super(null);
        providerRoomDao = db.providerRoomDAO();
    }

    /**
     * Instantiates a new Provider repository.
     *
     * @param restApi the rest api
     * @param logger  the logger
     */
    public ProviderRepository(RestApi restApi, OpenMRSLogger logger) {
        super(restApi, logger);
    }

    /**
     * Sets provider room dao.
     *
     * @param providerRoomDao the provider room dao
     */
    public void setProviderRoomDao(ProviderRoomDAO providerRoomDao) {
        this.providerRoomDao = providerRoomDao;
    }

    /**
     * Gets location.
     *
     * @param url the url
     * @return a list of location entities
     */
    public Observable<List<LocationEntity>> getLocations(String url) {
        return AppDatabaseHelper.createObservableIO(() -> {
            String locationEndPoint = url + ApplicationConstants.API.REST_ENDPOINT + "location";
            Response<Results<LocationEntity>> response =
                    restApi.getLocations(locationEndPoint, ApplicationConstants.API.TAG_ADMISSION_LOCATION, ApplicationConstants.API.FULL).execute();
            if (response.isSuccessful()) return response.body().getResults();
            else throw new Exception("fetch provider location error: " + response.message());
        });
    }

    /**
     * Gets encounter roles.
     *
     * @return a list of resources of encounter roles
     */
    public Observable<List<Resource>> getEncounterRoles() {
        return AppDatabaseHelper.createObservableIO(() -> {
            Response<Results<Resource>> response = restApi.getEncounterRoles().execute();
            if (response.isSuccessful()) return response.body().getResults();
            else throw new Exception("fetch encounter roles error: " + response.message());
        });
    }
}
