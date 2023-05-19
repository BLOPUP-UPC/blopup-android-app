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

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.dashboard.DashboardActivity
import edu.upc.openmrs.application.OpenMRS
import edu.upc.sdk.utilities.ApplicationConstants


object NotificationUtil {

    @JvmStatic
    fun showRecordingNotification(title: String, message: String) {
        val mNotificationManager = OpenMRS.getInstance()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ApplicationConstants.LEGAL_CONSENT_RECORDING,
                ApplicationConstants.LEGAL_CONSENT,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.BLUE
            channel.description = ApplicationConstants.LEGAL_NOTIFICATION_CHANNEL_DESCRIPTION
            mNotificationManager.createNotificationChannel(channel)
        }

        val bitmap = BitmapFactory.decodeResource(
            OpenMRS.getInstance().resources,
            R.drawable.recording
        )

        val mBuilder =
            NotificationCompat.Builder(OpenMRS.getInstance().baseContext, "legal_consent_recording")
                .setSmallIcon(R.mipmap.ico_vitals) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true) // clear notification after click
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setLargeIcon(bitmap)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val intent = Intent(OpenMRS.getInstance().baseContext, AddEditPatientActivity::class.java)

        val pi = PendingIntent.getActivity(
            OpenMRS.getInstance().baseContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //val action: Notification.Action = NotificationCompat.Action(icon, title, pendingIntent)

        mBuilder.setContentIntent(pi)
        mNotificationManager.notify(0, mBuilder.build())
    }
}