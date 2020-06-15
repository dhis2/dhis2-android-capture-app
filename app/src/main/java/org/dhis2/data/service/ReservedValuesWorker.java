package org.dhis2.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.dhis2.App;
import org.dhis2.R;

import java.util.Objects;

import javax.inject.Inject;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class ReservedValuesWorker extends Worker {

    private final static String rv_channel = "sync_rv_notification";
    private final static int SYNC_RV_ID = 8071987;

    @Inject
    SyncPresenter presenter;

    public ReservedValuesWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Objects.requireNonNull(((App) getApplicationContext()).userComponent())
                .plus(new ReservedValuesWorkerModule()).inject(this);

        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                "syncing reserved values");

        try {
            presenter.syncReservedValues();
        } catch (Exception e) {
            Timber.e(e);
        }

        cancelNotification();

        return Result.success();
    }

    private void triggerNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(rv_channel, "ReservedValuesSync", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), rv_channel)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        notificationManager.notify(ReservedValuesWorker.SYNC_RV_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_RV_ID);
    }
}
