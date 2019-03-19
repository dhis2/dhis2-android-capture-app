package org.dhis2.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.firebase.perf.metrics.AddTrace;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;

import java.util.Calendar;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    @AddTrace(name = "MetadataSyncTrace")
    public Result doWork() {
        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new SyncDataWorkerModule()).inject(this);

        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_data));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", true));

        boolean isEventOk = true;
        boolean isTeiOk = true;
        boolean isDataValue = true;
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

        try {
            presenter.syncAndDownloadDataValues();
        } catch (Exception e) {
            Timber.e(e);
            isDataValue = false;
        }

        String lastDataSyncDate = DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime());

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.LAST_DATA_SYNC, lastDataSyncDate).apply();
        prefs.edit().putBoolean(Constants.LAST_DATA_SYNC_STATUS, isEventOk && isTeiOk && isDataValue).apply();

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", false));

        cancelNotification();

        return Result.SUCCESS;
    }

    private void triggerNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(data_channel, "DataSync", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), data_channel)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(SyncDataWorker.SYNC_DATA_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_DATA_ID);
    }
}
