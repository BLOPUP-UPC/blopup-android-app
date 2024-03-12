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
package edu.upc.sdk.library

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * The type Open mrs logger.
 */
class OpenMRSLogger {
    private val androidDefaultUEH: Thread.UncaughtExceptionHandler

    /**
     * Instantiates a new Open mrs logger.
     */
    init {
        logger = this
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler()!!
        val handler = Thread.UncaughtExceptionHandler { thread: Thread?, ex: Throwable? ->
            logger!!.e("Uncaught exception is: ", ex)
            if (thread != null) {
                if (ex != null) {
                    androidDefaultUEH.uncaughtException(thread, ex)
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }

    /**
     * V.
     *
     * @param msg the msg
     */
    fun v(msg: String) {
        Log.v(mTAG, getMessage(msg))
        Firebase.crashlytics
    }

    /**
     * V.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    fun v(msg: String, tr: Throwable?) {
        Log.v(mTAG, getMessage(msg), tr)
    }

    /**
     * D.
     *
     * @param msg the msg
     */
    fun d(msg: String) {
        if (IS_DEBUGGING_ON) {
            Log.d(mTAG, getMessage(msg))
        }
    }

    /**
     * D.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    fun d(msg: String, tr: Throwable?) {
        if (IS_DEBUGGING_ON) {
            Log.d(mTAG, getMessage(msg), tr)
        }
    }

    /**
     * .
     *
     * @param msg the msg
     */
    fun i(msg: String) {
        Log.i(mTAG, getMessage(msg))
        Firebase.crashlytics.log(msg)
    }

    /**
     * .
     *
     * @param msg the msg
     * @param tr  the tr
     */
    fun i(msg: String, tr: Throwable?) {
        Log.i(mTAG, getMessage(msg), tr)
        Firebase.crashlytics.log("$msg: ${tr?.message}")
    }

    /**
     * W.
     *
     * @param msg the msg
     */
    fun w(msg: String) {
        Log.w(mTAG, getMessage(msg))
        Firebase.crashlytics.log(msg)
    }

    /**
     * E.
     *
     * @param msg the msg
     */
    fun e(msg: String) {
        Log.e(mTAG, getMessage(msg))
        Firebase.crashlytics.log(msg)
    }

    /**
     * E.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    fun e(msg: String, tr: Throwable?) {
        Log.e(mTAG, getMessage(msg), tr)
        Firebase.crashlytics.log("$msg: ${tr?.message}")
    }

    companion object {
        private const val mTAG = "OpenMRS"
        private const val IS_DEBUGGING_ON = false
        private var logger: OpenMRSLogger? = null
        private fun getMessage(msg: String): String {
            val fullClassName = Thread.currentThread().getStackTrace()[4].className
            val className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1)
            val methodName = Thread.currentThread().getStackTrace()[4].methodName
            val lineNumber = Thread.currentThread().getStackTrace()[4].lineNumber
            return "#$lineNumber $className.$methodName() : $msg"
        }
    }
}
