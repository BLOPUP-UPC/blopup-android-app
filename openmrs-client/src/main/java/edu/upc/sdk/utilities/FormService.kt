/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package edu.upc.sdk.utilities

import com.google.gson.GsonBuilder
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.databases.entities.FormResourceEntity
import edu.upc.sdk.library.models.Form
import edu.upc.sdk.utilities.StringUtils.isBlank
import edu.upc.sdk.utilities.StringUtils.unescapeJavaString
import java.lang.reflect.Modifier

object FormService {

    @JvmStatic
    fun getForm(valueReference: String?): Form {
        val unescapedValueReference = unescapeJavaString(valueReference!!)
        val builder = GsonBuilder()
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
        builder.excludeFieldsWithoutExposeAnnotation()
        val gson = builder.create()
        return gson.fromJson(unescapedValueReference, Form::class.java)
    }

    @JvmStatic
    fun getFormByUuid(uuid: String?): Form? {
        if (!isBlank(uuid)) {
            var formResourceEntity : FormResourceEntity?
            try {
                formResourceEntity = AppDatabase.getDatabase(
                    OpenmrsAndroid.getInstance()?.applicationContext
                )
                        .formResourceDAO()
                        .getFormByUuid(uuid)
                        .blockingGet()
            } catch (e: Exception) {
                formResourceEntity = null
            }
            if (formResourceEntity != null) {
                val resourceList = formResourceEntity.resources
                for (resource in resourceList) {
                    if ("json" == resource.name) {
                        val valueRefString = resource.valueReference
                        val form = getForm(valueRefString)
                        form.valueReference = valueRefString
                        form.name = formResourceEntity.name
                        return form
                    }
                }
            }
        }
        return null
    }

}
