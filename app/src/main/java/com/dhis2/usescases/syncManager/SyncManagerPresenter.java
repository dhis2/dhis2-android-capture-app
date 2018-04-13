package com.dhis2.usescases.syncManager;

import android.content.Intent;

import com.dhis2.data.service.SyncDataService;
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.data.service.SyncService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerPresenter implements SyncManagerContracts.Presenter{

    private SyncManagerContracts.View view;

    public SyncManagerPresenter(SyncManagerContracts.View view) {
        this.view = view;
    }


    @Override
    public void syncData() {
        String tag = "data";
        FirebaseJobDispatcher dispatcher;
        Job dataJob = null;
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(view.getContext()));
        //if (dataJob != null) dispatcher.cancel(tag);
        dataJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(SyncDataService.class)
                // uniquely identifies the job
                .setTag(tag)
                // one-off job
                //.setRecurring(true)
                // start between - and - seconds from now
                //.setTrigger(Trigger.executionWindow(0, 150))
                // don't overwrite an existing job with the same tag
                //.setReplaceCurrent(false)
                // don't persist past a device reboot
                //.setLifetime(Lifetime.FOREVER)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.mustSchedule(dataJob);

    //    view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncDataService.class));
    }

    @Override
    public void syncMeta() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncMetadataService.class));
    }
}
