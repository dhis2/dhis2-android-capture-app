package com.dhis2.data.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.schedulers.SchedulerProvider;

import org.hisp.dhis.android.core.D2;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import rx.exceptions.OnErrorNotImplementedException;

final class SyncPresenterImpl implements SyncPresenter {

    @NonNull
    private final D2 d2;

    @NonNull
    private final SchedulerProvider schedulerProvider;

    @NonNull
    private final CompositeDisposable disposable;

    @Nullable
    private SyncView syncView;

    SyncPresenterImpl(@NonNull D2 d2, @NonNull SchedulerProvider schedulerProvider) {
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
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
                .subscribe(update(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        disposable.add(events()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        disposable.add(trackerData()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @Override
    public void onDetach() {
        disposable.clear();
        syncView = null;
    }

    @NonNull
    private Observable<Response> metadata() {
        return Observable.defer(() -> Observable.fromCallable(d2.syncMetaData()));
    }

    @NonNull
    private Observable<Response> trackerData() {
        return Observable.defer(() -> Observable.fromCallable(d2.syncTrackerData()));
    }

    @NonNull
    private Observable<Response> events() {
        return Observable.defer(() -> Observable.fromCallable(d2.syncSingleData(300)));
    }


    @NonNull
    private Consumer<SyncResult> update() {
        return result -> {
            if (syncView != null) {
                syncView.update().accept(result);
            }
        };
    }
}
