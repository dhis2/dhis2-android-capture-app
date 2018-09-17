package org.dhis2.data.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.dhis2.utils.Constants;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

final class SyncPresenterImpl implements SyncPresenter {

    @NonNull
    private final D2 d2;

    @NonNull
    private final CompositeDisposable disposable;

    @Nullable
    private SyncView syncView;

    SyncPresenterImpl(@NonNull D2 d2) {
        this.d2 = d2;
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onAttach(@NonNull SyncView view) {

        syncView = view;

    }

    @Override
    public void sync() {

        disposable.add(metadata()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncState.METADATA),
                        Timber::d
                ));
    }

    @Override
    public void syncMetaData() {

        disposable.add(metadata()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncState.METADATA),
                        Timber::d));
    }

    @Override
    public void syncEvents() {

//        new PostData().execute();

        disposable.add(Observable.fromCallable(d2.syncSingleEvents())
                .map(webResponse -> SyncResult.success())
                .onErrorReturn(throwable -> SyncResult.failure(throwable.getMessage()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        data -> Log.d("DONE", "DONE"),
                        Timber::d
                )
        );

        disposable.add(
                events()
                        .subscribeOn(Schedulers.io())
                        .map(response -> SyncResult.success())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn(throwable -> SyncResult.failure(
                                throwable.getMessage() == null ? "" : throwable.getMessage()))
                        .startWith(SyncResult.progress())
                        .subscribe(update(SyncState.EVENTS),
                                Timber::d));
    }

    @Override
    public void syncTrackedEntities() {


        disposable.add(
                trackerData()
                        .subscribeOn(Schedulers.io())
                        .map(response -> SyncResult.success())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn(throwable -> SyncResult.failure(
                                throwable.getMessage() == null ? "" : throwable.getMessage()))
                        .startWith(SyncResult.progress())
                        .subscribe(update(SyncState.TEI),
                                Timber::d
                        ));

    }

    @Override
    public void onDetach() {
        disposable.clear();
        syncView = null;
    }

    @NonNull
    private Observable<Unit> metadata() {
        return Observable.defer(() -> Observable.fromCallable(d2.syncMetaData()));
    }

    @NonNull
    private Observable<List<TrackedEntityInstance>> trackerData() {
        SharedPreferences prefs = syncView.getContext().getSharedPreferences(
                "org.dhis2", Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        return Observable.defer(() -> Observable.fromCallable(d2.downloadTrackedEntityInstances(teiLimit, limityByOU)));
    }

    @NonNull
    private Observable<List<Event>> events() {
        SharedPreferences prefs = syncView.getContext().getSharedPreferences(
                "org.dhis2", Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        return Observable.defer(() -> Observable.fromCallable(d2.downloadSingleEvents(eventLimit, limityByOU)));
    }


    @NonNull
    private Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
            if (syncView != null) {
                syncView.update(syncState).accept(result);
            }
        };
    }

    class PostData extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                d2.syncSingleEvents().call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                d2.syncTrackedEntityInstances().call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
