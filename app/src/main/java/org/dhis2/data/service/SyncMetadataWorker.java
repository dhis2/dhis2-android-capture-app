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
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.D2Manager;

import java.util.Calendar;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 23/10/2018.
 */

public class SyncMetadataWorker extends Worker {

    private static final String METADATA_CHANNEL = "sync_metadata_notification";
    private static final int SYNC_METADATA_ID = 26061987;

    @Inject
    SyncPresenter presenter;

    public SyncMetadataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @AddTrace(name = "MetadataSyncTrace")
    public Result doWork() {

        Timber.d("USER COMPONENT IS NULL : %s", ((App) getApplicationContext()).userComponent() != null);
        Timber.d("SERVER COMPONENT IS NULL : %s", ((App) getApplicationContext()).serverComponent() != null);
        try {
            Timber.d("D2 IS NULL : %s", D2Manager.getD2() != null);
        } catch (IllegalStateException e) {
            Timber.d("D2 : %s", e.getMessage());

        }

        if (((App) getApplicationContext()).userComponent() != null) {

            ((App) getApplicationContext()).userComponent().plus(new SyncMetadataWorkerModule()).inject(this);

            triggerNotification(
                    getApplicationContext().getString(R.string.app_name),
                    getApplicationContext().getString(R.string.syncing_configuration),
                    0);

            boolean isMetaOk = true;
            boolean noNetwork = false;

            try {
                presenter.syncMetadata(progress -> triggerNotification(
                        getApplicationContext().getString(R.string.app_name),
                        getApplicationContext().getString(R.string.syncing_configuration),
                        progress));
            } catch (Exception e) {
                Timber.e(e);
                isMetaOk = false;
                if (!NetworkUtils.isOnline(getApplicationContext()))
                    noNetwork = true;
            }

            String lastDataSyncDate = DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime());

            SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            prefs.edit().putString(Constants.LAST_META_SYNC, lastDataSyncDate).apply();
            prefs.edit().putBoolean(Constants.LAST_META_SYNC_STATUS, isMetaOk).apply();
            prefs.edit().putBoolean(Constants.LAST_META_SYNC_NO_NETWORK, noNetwork).apply();

            cancelNotification();

            if (!isMetaOk)
                return Result.failure(createOutputData(false));

            presenter.startPeriodicMetaWork();

            return Result.success(createOutputData(true));
        } else {
            return Result.failure(createOutputData(false));
        }
    }

    private Data createOutputData(boolean state) {
        return new Data.Builder()
                .putBoolean("METADATA_STATE", state)
                .build();
    }


    private void triggerNotification(String title, String content, int progress) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(METADATA_CHANNEL, "MetadataSync", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), METADATA_CHANNEL)
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setProgress(100, progress, false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(SyncMetadataWorker.SYNC_METADATA_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(SYNC_METADATA_ID);
    }

    public interface OnProgressUpdate {
        void onProgressUpdate(int progress);
    }
}
