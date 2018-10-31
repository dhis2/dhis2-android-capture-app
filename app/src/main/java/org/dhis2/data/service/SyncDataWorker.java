package org.dhis2.data.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;

import java.util.Calendar;

import javax.inject.Inject;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class SyncDataWorker extends Worker {

    private final static String data_channel = "sync_data_notification";
    private final static int SYNC_DATA_ID = 8071986;

    @Inject
    SyncPresenter presenter;

    public SyncDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ((App) getApplicationContext()).userComponent().plus(new SyncDataWorkerModule()).inject(this);

        triggerNotification(SYNC_DATA_ID,
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_data));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", true));

        boolean isEventOk = true;
        boolean isTeiOk = true;

        try {
            presenter.syncAndDownloadEvents(getApplicationContext());
        } catch (Exception e) {
            Timber.e(e);
            isEventOk = false;
        }
        try {
            presenter.syncAndDownloadTeis(getApplicationContext());
        } catch (Exception e) {
            Timber.e(e);
            isTeiOk = false;
        }

        String lastDataSyncDate = DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime());

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.LAST_DATA_SYNC, lastDataSyncDate).apply();
        prefs.edit().putBoolean(Constants.LAST_DATA_SYNC_STATUS, isEventOk && isTeiOk).apply();

        Log.d(this.getClass().getSimpleName(),"Last data sync at: "+lastDataSyncDate);
        Log.d(this.getClass().getSimpleName(),"Last data sync saved: "+prefs.getString(Constants.LAST_DATA_SYNC,"Not saved"));

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", false));

        cancelNotification();

        return Result.SUCCESS;
    }

    private void triggerNotification(int id, String title, String content) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), data_channel)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        notificationManager.notify(id, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_DATA_ID);
    }
}
