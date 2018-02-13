package com.dhis2.data.service;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.domain.responses.TEIResponse;
import com.dhis2.domain.responses.TrackedEntityInstance;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.enrollment.EnrollmentHandler;
import org.hisp.dhis.android.core.enrollment.EnrollmentStoreImpl;
import org.hisp.dhis.android.core.event.EventHandler;
import org.hisp.dhis.android.core.event.EventStoreImpl;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceStoreImpl;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.SystemInfoCall;
import org.hisp.dhis.android.core.systeminfo.SystemInfoService;
import org.hisp.dhis.android.core.systeminfo.SystemInfoStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueStoreImpl;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceEndPointCall;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceStoreImpl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.flowable.FlowableInterval;
import io.reactivex.internal.operators.flowable.FlowableTimer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import rx.exceptions.OnErrorNotImplementedException;

final class SyncPresenterImpl implements SyncPresenter {

    @NonNull
    private final D2 d2;

    @NonNull
    private final CompositeDisposable disposable;

    @Nullable
    private SyncView syncView;

    private List<TrackedEntityInstance> teis;

    private int position;

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
                .subscribe(update(SyncService.SyncState.METADATA), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @Override
    public void syncEvents() {
        disposable.add(events()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncService.SyncState.EVENTS), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @Override
    public void syncTrackedEntities() {

        getTEI();

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
        return Observable.defer(() -> Observable.fromCallable(d2.syncTrackedEntityInstances()));
    }

    @NonNull
    private Observable<Response> trackerData2(String uid) {
        return Observable.defer( ()-> Observable.fromCallable(d2.syncTEI(uid)));
    }

    @NonNull
    private Observable<Response> events() {
        return Observable.defer(() -> Observable.fromCallable(d2.syncSingleData(600)));
    }


    @NonNull
    private Consumer<SyncResult> update(SyncService.SyncState syncState) {
        return result -> {
            if (syncView != null) {
                syncView.update(syncState).accept(result);
            }
        };
    }

    void getTEI() {
        //TODO: TEI sync not working. Get all TEI uids and call external method before SyncService starts
        d2.retrofit().create(TESTTrackedEntityInstanceService.class).trackEntityInstances().enqueue(new Callback<TEIResponse>() {
            @Override
            public void onResponse(Call<TEIResponse> call, Response<TEIResponse> response) {
                teis = response.body().getTrackedEntityInstances();
                position = 0;
                syncTEI();
            }

            @Override
            public void onFailure(Call<TEIResponse> call, Throwable t) {
            }
        });
    }

    void syncTEI() {

        disposable.add(Flowable.just(teis)
                .flatMap(FlowableInterval::fromIterable)
                .flatMap(tei -> trackerData2(tei.getTrackedEntityInstance()).onErrorResumeNext(Observable.empty()).toFlowable(BackpressureStrategy.BUFFER))
                .map(mresponse -> SyncResult.success())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncService.SyncState.TEI)
                        , throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));
    }

    //TODO: Currentl, SDK not providing TEI sync. This call is used for user android in android-current
    public interface TESTTrackedEntityInstanceService {
        @GET("28/trackedEntityInstances?ou=ImspTQPwCqd&ouMode=ACCESSIBLE&totalPages=true&paging=false&fields=trackedEntityInstance")
        Call<TEIResponse> trackEntityInstances();
    }

    class TEIAsync extends AsyncTask<String, String, Void> {

        private List<TrackedEntityInstance> list;
        DatabaseAdapter databaseAdapter;
        TrackedEntityInstanceStoreImpl trackedEntityInstanceStore;
        TrackedEntityAttributeValueHandler trackedEntityAttributeValueHandler;
        TrackedEntityDataValueHandler trackedEntityDataValueHandler;
        EnrollmentHandler enrollmentHandler;
        TrackedEntityInstanceHandler trackedEntityInstanceHandler;
        ResourceHandler resourceHandler;

        public TEIAsync(List<TrackedEntityInstance> trackedEntityInstances) {
            this.list = trackedEntityInstances;
            databaseAdapter = d2.databaseAdapter();

            trackedEntityInstanceStore =
                    new TrackedEntityInstanceStoreImpl(databaseAdapter);

            trackedEntityAttributeValueHandler =
                    new TrackedEntityAttributeValueHandler(new TrackedEntityAttributeValueStoreImpl(databaseAdapter));

            trackedEntityDataValueHandler =
                    new TrackedEntityDataValueHandler(new TrackedEntityDataValueStoreImpl(databaseAdapter));

            enrollmentHandler = new EnrollmentHandler(
                    new EnrollmentStoreImpl(databaseAdapter), new EventHandler(
                    new EventStoreImpl(databaseAdapter), trackedEntityDataValueHandler));

            trackedEntityInstanceHandler =
                    new TrackedEntityInstanceHandler(
                            trackedEntityInstanceStore,
                            trackedEntityAttributeValueHandler,
                            enrollmentHandler);

            resourceHandler = new ResourceHandler(new ResourceStoreImpl(databaseAdapter));


        }

        @Override
        protected Void doInBackground(String... strings) {

            saveTEI(list, databaseAdapter, trackedEntityInstanceHandler, resourceHandler);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            syncEvents();

            /*disposable.add(trackerData()
                    .subscribeOn(Schedulers.io())
                    .map(response -> SyncResult.success())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> SyncResult.failure(
                            throwable.getMessage() == null ? "" : throwable.getMessage()))
                    .startWith(SyncResult.progress())
                    .subscribe(update(SyncService.SyncState.TEI), throwable -> {
                        throw new OnErrorNotImplementedException(throwable);
                    }));*/
        }
    }

    private void saveTEI(List<TrackedEntityInstance> trackedEntityInstances,
                         DatabaseAdapter databaseAdapter,
                         TrackedEntityInstanceHandler trackedEntityInstanceHandler,
                         ResourceHandler resourceHandler) {

        Transaction transaction = databaseAdapter.beginNewTransaction();

        Response response = null;
        try {

            response = new SystemInfoCall(
                    databaseAdapter,
                    new SystemInfoStoreImpl(databaseAdapter),
                    d2.retrofit().create(SystemInfoService.class),
                    new ResourceStoreImpl(databaseAdapter)
            ).call();

            if (!response.isSuccessful()) {
                return;
            }

            SystemInfo systemInfo = (SystemInfo) response.body();
            Date serverDate = systemInfo.serverDate();

            for (int i = 0; i < trackedEntityInstances.size(); i++) {
                try {
                    response = new TrackedEntityInstanceEndPointCall(
                            d2.retrofit().create(TrackedEntityInstanceService.class),
                            databaseAdapter,
                            trackedEntityInstanceHandler,
                            resourceHandler,
                            serverDate,
                            trackedEntityInstances.get(i).getTrackedEntityInstance()
                    ).call();
                } catch (Exception e) {
                    Log.d("TEI ERROR", trackedEntityInstances.get(i).getTrackedEntityInstance() + " - " + e.getMessage());
                }
            }
            transaction.setSuccessful();

        } catch (Exception e) {
            Log.d("TEI RESPONSE", "ERROR SERVER RESPONSE");
        } finally {
            String data = response != null ? String.valueOf(response.isSuccessful()) : "false";
            Log.d("TEI RESPONSE", "IS SUCCSESS? = " + data);
            transaction.end();

        }
    }
}
