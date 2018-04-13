package com.dhis2.usescases.syncManager;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.service.SyncDataService;
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.usescases.main.MainContracts;
import com.dhis2.utils.DateUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerPresenter implements SyncManagerContracts.Presenter{

    public static final int DATA_RESOURCE = 16;  //Events Resource
    public static final int META_DATA_RESOURCE = 1; // User Resource

    private MetadataRepository metadataRepository;
    private FirebaseJobDispatcher dispatcher;
    private CompositeDisposable compositeDisposable;

    public SyncManagerPresenter(MetadataRepository metadataRepository, FirebaseJobDispatcher dispatcher) {
        this.metadataRepository = metadataRepository;
        this.dispatcher = dispatcher;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(SyncManagerContracts.View view) {
        compositeDisposable.add(
                metadataRepository.getLastSync(META_DATA_RESOURCE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resourceModel -> view.setLastMetaDataSyncDate(DateUtils.dateTimeFormat().format(resourceModel.lastSynced())),
                        Timber::e
                )
        );

        compositeDisposable.add(
                metadataRepository.getLastSync(DATA_RESOURCE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resourceModel -> view.setLastDataSyncDate(DateUtils.dateTimeFormat().format(resourceModel.lastSynced())),
                                Timber::e
                        )
        );
    }

    @Override
    public void syncData(int seconds) {
        String tag = "Data";
        Job dataJob;

        boolean isRecurring = false;
        JobTrigger trigger = Trigger.NOW;

        if(seconds != 0){
            isRecurring = true;
            trigger = Trigger.executionWindow(seconds, seconds + 60);
        }

        dataJob = dispatcher.newJobBuilder()
                .setService(SyncDataService.class)
                .setTag(tag)
                .setRecurring(isRecurring)
                .setTrigger(trigger)
                .setReplaceCurrent(false)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .build();

        dispatcher.mustSchedule(dataJob);
    }

    @Override
    public void syncMeta(int seconds) {
        String tag = "MetaData";
        Job metaJob;

        boolean isRecurring = false;
        JobTrigger trigger = Trigger.NOW;

        if(seconds != 0){
            isRecurring = true;
            trigger = Trigger.executionWindow(seconds, seconds + 60);
        }

        metaJob = dispatcher.newJobBuilder()
                .setService(SyncMetadataService.class)
                .setTag(tag)
                .setRecurring(isRecurring)
                .setTrigger(trigger)
                .setReplaceCurrent(true)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .build();
        dispatcher.mustSchedule(metaJob);
    }
}
