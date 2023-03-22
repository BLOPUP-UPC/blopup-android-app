package edu.upc.openmrs.services;

import static edu.upc.sdk.utilities.ApplicationConstants.ConceptDownloadService.CHANNEL_DESC;
import static edu.upc.sdk.utilities.ApplicationConstants.ConceptDownloadService.CHANNEL_ID;
import static edu.upc.sdk.utilities.ApplicationConstants.ConceptDownloadService.CHANNEL_NAME;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.ConceptRoomDAO;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.entities.ConceptEntity;
import edu.upc.sdk.library.models.Link;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.SystemSetting;
import edu.upc.sdk.utilities.ApplicationConstants;

import java.util.List;

import edu.upc.R;
import edu.upc.openmrs.activities.settings.SettingsActivity;
import edu.upc.openmrs.application.OpenMRS;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConceptDownloadService extends Service {
    private int downloadedConcepts;
    private int maxConceptsInOneQuery = 100;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ApplicationConstants.ServiceActions.START_CONCEPT_DOWNLOAD_ACTION)) {
            showNotification(downloadedConcepts);
            startDownload();
            downloadConcepts(downloadedConcepts);
        } else if (intent.getAction().equals(
                ApplicationConstants.ServiceActions.STOP_CONCEPT_DOWNLOAD_ACTION)) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void startDownload() {
        RestApi service = RestServiceBuilder.createService(RestApi.class);
        Call<Results<SystemSetting>> call = service.getSystemSettingsByQuery(
                ApplicationConstants.SystemSettingKeys.WS_REST_MAX_RESULTS_ABSOLUTE,
                ApplicationConstants.API.FULL);
        call.enqueue(new Callback<Results<SystemSetting>>() {
            @Override
            public void onResponse(@NonNull Call<Results<SystemSetting>> call, @NonNull Response<Results<SystemSetting>> response) {
                if (response.isSuccessful()) {
                    List<SystemSetting> results = null;
                    if (response.body() != null) {
                        results = response.body().getResults();
                        if (results.size() >= 1) {
                            String value = results.get(0).getValue();
                            if (value != null) {
                                maxConceptsInOneQuery = Integer.parseInt(value);
                            }
                        }
                    }
                }
                downloadConcepts(0);
            }

            @Override
            public void onFailure(@NonNull Call<Results<SystemSetting>> call, @NonNull Throwable t) {
                downloadConcepts(0);
            }
        });
    }

    private void showNotification(int downloadedConcepts) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelPayment = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channelPayment.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channelPayment);
        }
        final int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(ApplicationConstants.BroadcastActions.CONCEPT_DOWNLOAD_BROADCAST_INTENT_KEY_COUNT, downloadedConcepts);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flag);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_openmrs);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.downloading_concepts_notification_message))
                .setTicker(getString(R.string.app_name))
                .setContentText(String.valueOf(downloadedConcepts))
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(ApplicationConstants.ServiceNotificationId.CONCEPT_DOWNLOADFOREGROUND_SERVICE,
                notification);
    }

    private void downloadConcepts(int startIndex) {
        RestApi service = RestServiceBuilder.createService(RestApi.class);
        Call<Results<ConceptEntity>> call = service.getConcepts(maxConceptsInOneQuery, startIndex);
        call.enqueue(new Callback<Results<ConceptEntity>>() {
            @Override
            public void onResponse(@NonNull Call<Results<ConceptEntity>> call, @NonNull Response<Results<ConceptEntity>> response) {
                if (response.isSuccessful()) {
                    ConceptRoomDAO conceptDAO = AppDatabase.getDatabase(OpenMRS.getInstance().getApplicationContext()).conceptRoomDAO();
                    if (response.body() != null) {
                        for (ConceptEntity concept : response.body().getResults()) {
                            conceptDAO.addConcept(concept);
                            downloadedConcepts++;
                        }
                    }

                    showNotification(downloadedConcepts);
                    sendProgressBroadcast();

                    boolean isNextPage = false;
                    if (response.body() != null) {
                        for (Link link : response.body().getLinks()) {
                            if ("next".equals(link.getRel())) {
                                isNextPage = true;
                                downloadConcepts(startIndex + maxConceptsInOneQuery);
                                break;
                            }
                        }
                    }
                    if (!isNextPage) {
                        stopSelf();
                    }
                } else {
                    stopSelf();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Results<ConceptEntity>> call, @NonNull Throwable t) {
                stopSelf();
            }
        });
    }

    private void sendProgressBroadcast() {
        Intent intent = new Intent(ApplicationConstants.BroadcastActions.CONCEPT_DOWNLOAD_BROADCAST_INTENT_ID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
