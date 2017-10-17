package com.dhis2.data.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.data.schedulers.SchedulerProvider;

import org.hisp.dhis.android.core.D2;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
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

        syncView = (SyncView) view;

    }

    @Override
    public void sync() {
        disposable.add(metadata()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .map(response -> SyncResult.success())
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
    private Consumer<SyncResult> update() {
        return result -> {
            if (syncView != null) {
                syncView.update().accept(result);
            }
        };
    }
}
