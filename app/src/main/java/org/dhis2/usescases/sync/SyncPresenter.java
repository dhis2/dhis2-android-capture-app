package org.dhis2.usescases.sync;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.service.ReservedValuesWorker;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SyncPresenter implements SyncContracts.Presenter {

    private final MetadataRepository metadataRepository;
    private SyncContracts.View view;

    private CompositeDisposable disposable;


    SyncPresenter(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(SyncContracts.View view) {
        this.view = view;
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public void syncData(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    public void getTheme() {
        disposable.add(metadataRepository.getTheme()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flagTheme -> {
                            view.saveFlag(flagTheme.val0());
                            view.saveTheme(flagTheme.val1());
                        }, Timber::e
                ));

    }

/*
    @Override
    public void syncAggregatesData() {
        disposable.add(aggregatesData()
                .map(response -> SyncResult.success())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncActivity.SyncState.AGGREGATES),
                        throwable -> view.displayMessage(throwable.getMessage())
                ));
    }*/

    @Override
    public void syncReservedValues() {

        WorkManager.getInstance().cancelAllWorkByTag("TAG_RV");
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(ReservedValuesWorker.class);
        syncDataBuilder.addTag("TAG_RV");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);

    }

}