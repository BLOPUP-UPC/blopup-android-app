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

package edu.upc.sdk.library;

import android.util.Log;

/**
 * The type Open mrs logger.
 */
public class OpenMRSLogger {
    private static String mTAG = "OpenMRS";
    private static final boolean IS_DEBUGGING_ON = false;
    private static OpenMRSLogger logger = null;
    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    /**
     * Instantiates a new Open mrs logger.
     */
    public OpenMRSLogger() {
        logger = this;
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler handler = (thread, ex) -> {
            logger.e("Uncaught exception is: ", ex);
            androidDefaultUEH.uncaughtException(thread, ex);
        };
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    /**
     * V.
     *
     * @param msg the msg
     */
    public void v(final String msg) {
        Log.v(mTAG, getMessage(msg));
    }

    /**
     * V.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    public void v(final String msg, Throwable tr) {
        Log.v(mTAG, getMessage(msg), tr);
    }

    /**
     * D.
     *
     * @param msg the msg
     */
    public void d(final String msg) {
        if (IS_DEBUGGING_ON) {
            Log.d(mTAG, getMessage(msg));
        }
    }

    /**
     * D.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    public void d(final String msg, Throwable tr) {
        if (IS_DEBUGGING_ON) {
            Log.d(mTAG, getMessage(msg), tr);
        }
    }

    /**
     * .
     *
     * @param msg the msg
     */
    public void i(final String msg) {
        Log.i(mTAG, getMessage(msg));
    }

    /**
     * .
     *
     * @param msg the msg
     * @param tr  the tr
     */
    public void i(final String msg, Throwable tr) {
        Log.i(mTAG, getMessage(msg), tr);
    }

    /**
     * W.
     *
     * @param msg the msg
     */
    public void w(final String msg) {
        Log.w(mTAG, getMessage(msg));
    }


    /**
     * E.
     *
     * @param msg the msg
     */
    public void e(final String msg) {
        Log.e(mTAG, getMessage(msg));
    }

    /**
     * E.
     *
     * @param msg the msg
     * @param tr  the tr
     */
    public void e(final String msg, Throwable tr) {
        Log.e(mTAG, getMessage(msg), tr);
    }

    private static String getMessage(String msg) {
        final String fullClassName = Thread.currentThread().getStackTrace()[4].getClassName();
        final String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        final String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        final int lineNumber = Thread.currentThread().getStackTrace()[4].getLineNumber();

        return "#" + lineNumber + " " + className + "." + methodName + "() : " + msg;
    }
}
