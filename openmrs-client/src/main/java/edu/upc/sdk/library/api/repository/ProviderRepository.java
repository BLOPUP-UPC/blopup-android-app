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

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.dao.ProviderRoomDAO;

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
}
