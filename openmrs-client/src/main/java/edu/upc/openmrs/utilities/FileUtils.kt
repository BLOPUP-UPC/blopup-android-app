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
package edu.upc.openmrs.utilities

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import edu.upc.R
import edu.upc.openmrs.application.OpenMRS
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getRecordingFilePath(): String {
        return getRootDirectory() + "/" + createUniqueAudioFileName();
    }

    @JvmStatic
    fun getRootDirectory(): String {
        return OpenMRS.getInstance().applicationContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path.toString()
    }

    private fun createUniqueAudioFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return timeStamp + "_" + ".mp3"
    }

    @JvmStatic
    fun getFileByLanguage(activity: Activity?, fileName: String): Int {
        val lang = LanguageUtils.getLanguage()

        val resourceByLocal = activity?.resources?.getIdentifier(
            fileName + "_$lang", "raw", activity.packageName
        )

        return if (resourceByLocal == 0) R.raw.legal_consent_es else resourceByLocal!!
    }

    @JvmStatic
    fun fileIsCreatedSuccessfully(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) return false
        //check file is larger than 85KB
        return path?.let { File(it).length().div(1024) }!! > 85
    }
}