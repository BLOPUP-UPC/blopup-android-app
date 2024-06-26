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

package edu.upc.sdk.library.models

import androidx.room.Entity
import com.google.gson.annotations.Expose
import edu.upc.sdk.library.databases.entities.LocationEntity

/**
 * Visit
 *
 * <p> More on Visits https://rest.openmrs.org/#visits </p>
 * @constructor Create empty Visit
 */
@Entity
class OpenMRSVisit : Resource() {

    override var id: Long? = null

    @Expose
    lateinit var patient: Patient

    @Expose
    lateinit var visitType: VisitType

    @Expose
    var location: LocationEntity? = null

    @Expose
    lateinit var startDatetime: String

    @Expose
    var stopDatetime: String? = null

    @Expose
    lateinit var encounters: List<Encounter>
}
