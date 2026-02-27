package org.dhis2.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.dhis2.R;
import org.dhis2.commons.prefs.PreferenceProvider;

import javax.inject.Inject;

public class SyncDataWorker extends Worker {

    private static final String DATA_CHANNEL = "sync_data_notification";
    private static final int SYNC_DATA_ID = 8071986;

    @Inject
    SyncPresenter presenter;

    @Inject
    PreferenceProvider prefs;

    public SyncDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_data),
                0);

        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_events),
                20);


        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_teis),
                40);


        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_data_sets),
                60);


        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_resources),
                80);


        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                "syncing reserved values",
                95

        );


        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_done),
                100);

        cancelNotification();

        return Result.success(createOutputData(true));
    }

    @Override
    public void onStopped() {
        cancelNotification();
        super.onStopped();
    }

    private Data createOutputData(boolean state) {
        return new Data.Builder()
                .putBoolean("DATA_STATE", state)
                .build();
    }

    private void triggerNotification(String title, String content, int progress) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(DATA_CHANNEL, "DataSync", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), DATA_CHANNEL)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setProgress(100, progress, false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        setForegroundAsync(new ForegroundInfo(
                SyncDataWorker.SYNC_DATA_ID,
                notificationBuilder.build(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC : 0
        ));

    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_DATA_ID);
    }
}
