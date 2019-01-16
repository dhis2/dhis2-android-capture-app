package org.dhis2.data.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import org.dhis2.App;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class SyncDataService extends JobService implements SyncView {
    private final static int NOTIFICATION_ID = 0xdeadbeef;
    private final static int NOTIFICATION_ID_EVENT = 0xDEADBEEE;
    private final static int NOTIFICATION_ID_TEI = 0xDEADBEED;

    @Inject
    SyncPresenter syncPresenter;

    @Inject
    NotificationManager notificationManager;

    // @NonNull
    SyncResult syncResult;

    private JobParameters job;


    @Override
    public void onCreate() {
        super.onCreate();
        // inject dependencies

        Log.d(getClass().getSimpleName(), "DATA SERVICE ONCREATE");

        if (((App) getApplicationContext()).userComponent() == null)
            stopSelf();
        else
            ((App) getApplicationContext()).userComponent()
                    .plus(new DataServiceModule()).inject(this);
    }

    @Override
    public void onDestroy() {
        syncPresenter.onDetach();
        Log.d(getClass().getSimpleName(), "DATA SERVICE ONDESTROY");
        super.onDestroy();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        this.job = job;
        if (syncPresenter != null)
            syncPresenter.onAttach(this);
        syncResult = SyncResult.idle();
        if (!syncResult.inProgress()) {
            Log.d("SyncDataService", "Job tag " + job.getTag());
            Log.d("SyncDataService", "Job Started");
            syncPresenter.syncEvents();
        }
        return true; //Is there still work going on?
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Should this job be retried?
    }

    @NonNull
    @Override
    public Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
            syncResult = result;
            if (result.inProgress()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", true));

            } else if (result.isSuccess()) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", false));
                SharedPreferences prefs = getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.LAST_DATA_SYNC, DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime())).apply();
                prefs.edit().putBoolean(Constants.LAST_DATA_SYNC_STATUS, true).apply();

                next(syncState);
            } else if (!result.isSuccess()) { // NOPMD
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("action_sync").putExtra("dataSyncInProgress", false));
                SharedPreferences prefs = getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.LAST_DATA_SYNC, DateUtils.dateTimeFormat().format(Calendar.getInstance().getTime())).apply();
                prefs.edit().putBoolean(Constants.LAST_DATA_SYNC_STATUS, false).apply();
                next(syncState);
            } else {
                throw new IllegalStateException();
            }
        };
    }

    @NonNull
    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private void next(SyncState syncState) {
        switch (syncState) {
            case EVENTS:
                syncPresenter.syncTrackedEntities();
                break;
            case TEI:
                syncPresenter.onDetach();
                jobFinished(job, job.isRecurring());
                break;
        }
    }
}