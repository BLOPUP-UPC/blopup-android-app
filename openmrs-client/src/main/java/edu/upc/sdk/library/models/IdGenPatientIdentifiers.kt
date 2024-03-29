/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.sdk.library.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Id gen patient identifiers
 *
 * <p> More on Patient Identifier https://rest.openmrs.org/#patients-overview </p>
 * @constructor Create empty Id gen patient identifiers
 */
class IdGenPatientIdentifiers {

    @SerializedName("identifiers")
    @Expose
    var identifiers: List<String> = ArrayList()

}

