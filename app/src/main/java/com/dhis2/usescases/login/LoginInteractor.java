package com.dhis2.usescases.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;

import com.dhis2.App;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.data.server.UserManager;
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.utils.Constants;
import com.dhis2.utils.NetworkUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Response;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

import static com.dhis2.utils.Constants.TIME_DAILY;

public class LoginInteractor implements LoginContracts.Interactor {

    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;
    private ConfigurationRepository configurationRepository;
    private UserManager userManager;
    private FirebaseJobDispatcher dispatcher;

    @NonNull
    private final CompositeDisposable disposable;

    LoginInteractor(LoginContracts.View view, ConfigurationRepository configurationRepository, MetadataRepository metadataRepository, FirebaseJobDispatcher firebaseJobDispatcher) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.configurationRepository = configurationRepository;
        this.metadataRepository = metadataRepository;
        this.dispatcher = firebaseJobDispatcher;

    }


   /* @Override
    public void sync() {

        disposable.add(metadata()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(
                        update(LoginActivity.SyncState.METADATA),
                        throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));

    }*/

    public void syncEvents() {
        disposable.add(
                metadataRepository.getTheme()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(flagTheme -> {
                                    view.saveFlag(flagTheme.val0());
                                    view.saveTheme(flagTheme.val1());
                                },
                                Timber::e
                        ));

        disposable.add(events()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(LoginActivity.SyncState.EVENTS),
                        throwable -> view.displayMessage(throwable.getMessage())
                ));
    }

    public void syncTrackedEntities() {

        disposable.add(trackerData()
                .subscribeOn(Schedulers.io())
                .map(response -> {
//                    userManager.getD2().syncAllTrackedEntityAttributeReservedValues();
                    return SyncResult.success();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(LoginActivity.SyncState.TEI),
                        throwable -> view.displayMessage(throwable.getMessage())
                ));

    }

    public void syncReservedValues() {
        disposable.add(metadataRepository.getReserveUids()
                .map(pairs -> {
                    for (Pair<String, String> pair : pairs) {
                        userManager.getD2().popTrackedEntityAttributeReservedValue(pair.val0(), pair.val1());
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        data -> Timber.log(1, "DONE"),
                        Timber::d
                )
        );
    }

    @NonNull
    private Observable<Unit> metadata() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().syncMetaData()));
    }

    @NonNull
    private Observable<List<TrackedEntityInstance>> trackerData() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadTrackedEntityInstances(teiLimit, limityByOU)));
    }


    @NonNull
    private Observable<List<Event>> events() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);

        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadSingleEvents(eventLimit, limityByOU)));
    }

    @NonNull
    private Consumer<SyncResult> update(LoginActivity.SyncState syncState) {
        return result -> {
            if (view != null) {
                view.update(syncState).accept(result);
            }
        };
    }


    private void syncMetadata() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences("com.dhis2", Context.MODE_PRIVATE);
        prefs.edit().putInt("timeMeta", TIME_DAILY).apply();
        syncMeta(TIME_DAILY);
    }

    private void syncMeta(int seconds) {
        String tag = "MetaData";
        Job metaJob;

        //boolean isRecurring = false;
        JobTrigger trigger = Trigger.NOW;

        /*if (seconds != 0) {
            isRecurring = true;
            trigger = Trigger.executionWindow(seconds, seconds + 60);
        }*/

        metaJob = dispatcher.newJobBuilder()
                .setService(SyncMetadataService.class)
                .setTag(tag)
                .setRecurring(false)
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