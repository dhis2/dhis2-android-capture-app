package org.dhis2.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.perf.metrics.AddTrace;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;

import java.util.Calendar;
import java.util.Objects;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class SyncDataWorker extends Worker {

    private static final String DATA_CHANNEL = "sync_data_notification";
    private static final int SYNC_DATA_ID = 8071986;

    @Inject
    SyncPresenter presenter;

    public SyncDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @AddTrace(name = "DataSyncTrace")
    public Result doWork() {

        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new SyncDataWorkerModule()).inject(this);

        triggerNotification(
                getApplicationContext().getString(R.string.app_name),
                getApplicationContext().getString(R.string.syncing_data));

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
        boolean syncOk = presenter.checkSyncStatus();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.LAST_DATA_SYNC, lastDataSyncDate).apply();
        prefs.edit().putBoolean(Constants.LAST_DATA_SYNC_STATUS, isEventOk && isTeiOk && isDataValue && syncOk).apply();

        cancelNotification();

        return Result.success(createOutputData(true));
    }

    private Data createOutputData(boolean state) {
        return new Data.Builder()
                .putBoolean("DATA_STATE", state)
                .build();
    }

    private void triggerNotification(String title, String content) {
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
