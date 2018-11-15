package org.dhis2.data.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

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

    private final static String data_channel = "sync_data_notification";
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
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("action_sync_rv")
                        .putExtra("dataSyncInProgress", true)
                );

        try {
            presenter.syncReservedValues();
        } catch (Exception e) {
            Timber.e(e);
        }

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent("action_sync_rv")
                        .putExtra("rvSyncInProgress", false));

        cancelNotification();

        return Result.SUCCESS;
    }

    private void triggerNotification(String title, String content) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), data_channel)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        notificationManager.notify(ReservedValuesWorker.SYNC_RV_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_RV_ID);
    }
}
