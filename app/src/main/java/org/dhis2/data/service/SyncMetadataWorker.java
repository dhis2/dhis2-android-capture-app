package org.dhis2.data.service;

import static org.dhis2.data.service.SyncOutputKt.METADATA_MESSAGE;
import static org.dhis2.data.service.SyncOutputKt.METADATA_STATE;
import static org.dhis2.utils.analytics.AnalyticsConstants.METADATA_TIME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.maintenance.D2Error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;

import javax.inject.Inject;

import timber.log.Timber;

public class SyncMetadataWorker extends Worker {

    private static final String METADATA_CHANNEL = "sync_metadata_notification";
    private static final int SYNC_METADATA_ID = 26061987;

    @Inject
    SyncPresenter presenter;

    @Inject
    PreferenceProvider prefs;

    @Inject
    ResourceManager resourceManager;

    public SyncMetadataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (((App) getApplicationContext()).userComponent() != null) {

            ((App) getApplicationContext()).userComponent().plus(new SyncMetadataWorkerModule()).inject(this);

            triggerNotification(
                    getApplicationContext().getString(R.string.app_name),
                    getApplicationContext().getString(R.string.syncing_configuration),
                    0);

            boolean isMetaOk = true;
            boolean noNetwork = false;
            StringBuilder message = new StringBuilder("");

            long init = System.currentTimeMillis();
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
                if (e instanceof D2Error) {
                    D2Error error = (D2Error) e;
                    message.append(composeErrorMessageInfo(error));
                } else if (e.getCause() instanceof D2Error) {
                    D2Error error = (D2Error) e.getCause();
                    message.append(composeErrorMessageInfo(error));
                } else {
                    message.append(e.toString().split("\n\t")[0]);
                }
            } finally {
                presenter.logTimeToFinish(System.currentTimeMillis() - init, METADATA_TIME);
            }

            String lastDataSyncDate = DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime());

            prefs.setValue(Constants.LAST_META_SYNC, lastDataSyncDate);
            prefs.setValue(Constants.LAST_META_SYNC_STATUS, isMetaOk);
            prefs.setValue(Constants.LAST_META_SYNC_NO_NETWORK, noNetwork);

            cancelNotification();

            if (!isMetaOk)
                return Result.failure(createOutputData(false, message.toString()));

            presenter.startPeriodicMetaWork();

            return Result.success(createOutputData(true, message.toString()));
        } else {
            return Result.failure(createOutputData(false, getApplicationContext().getString(R.string.error_init_session)));
        }
    }

    @Override
    public void onStopped() {
        cancelNotification();
        super.onStopped();
    }

    private Data createOutputData(boolean state, String message) {
        return new Data.Builder()
                .putBoolean(METADATA_STATE, state)
                .putString(METADATA_MESSAGE, message)
                .build();
    }

    private String errorStackTrace(@Nullable Exception exception) {
        if (exception == null)
            return "";
        Writer writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private StringBuilder composeErrorMessageInfo(D2Error error) {
        StringBuilder builder = new StringBuilder("Cause: ")
                .append(resourceManager.parseD2Error(error))
                .append("\n\n")
                .append("Exception: ")
                .append(errorStackTrace((error).originalException()).split("\n\t")[0])
                .append("\n\n");

        if (error.created() != null) {
            builder.append("Created: ")
                    .append(error.created().toString())
                    .append("\n\n");
        }

        if (error.httpErrorCode() != null) {
            builder.append("Http Error Code: ")
                    .append(error.httpErrorCode())
                    .append("\n\n");
        }

        if (error.errorComponent() != null) {
            builder.append("Error component: ")
                    .append(error.errorComponent())
                    .append("\n\n");
        }

        if (error.url() != null) {
            builder.append("Url: ")
                    .append(error.url())
                    .append("\n\n");
        }

        builder.append("StackTrace: ")
                .append(errorStackTrace(error).split("\n\t")[0])
                .append("\n\n");

        return builder;
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

        setForegroundAsync(new ForegroundInfo(SyncMetadataWorker.SYNC_METADATA_ID, notificationBuilder.build()));
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
