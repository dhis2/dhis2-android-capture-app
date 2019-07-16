package org.dhis2.usescases.sync;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.service.ReservedValuesWorker;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.utils.Constants;

import java.util.concurrent.TimeUnit;

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
        if (seconds == 0) {
            OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncMetadataWorker.class);
            syncDataBuilder.addTag(Constants.META_NOW);
            syncDataBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            OneTimeWorkRequest request = syncDataBuilder.build();
            WorkManager.getInstance().beginUniqueWork(Constants.META_NOW, ExistingWorkPolicy.REPLACE, request).enqueue();
        } else {
            WorkManager.getInstance().cancelUniqueWork(scheduleTag);
            PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, seconds, TimeUnit.SECONDS);
            syncDataBuilder.addTag(scheduleTag);
            syncDataBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            PeriodicWorkRequest request = syncDataBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
        }
    }

    @Override
    public void syncData(int seconds, String scheduleTag) {
        if (seconds == 0) {
            OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncDataWorker.class);
            syncDataBuilder.addTag(Constants.DATA_NOW);
            syncDataBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            OneTimeWorkRequest request = syncDataBuilder.build();
            WorkManager.getInstance().beginUniqueWork(Constants.DATA_NOW, ExistingWorkPolicy.REPLACE, request).enqueue();
        } else {
            WorkManager.getInstance().cancelUniqueWork(scheduleTag);
            PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
            syncDataBuilder.addTag(scheduleTag);
            syncDataBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            PeriodicWorkRequest request = syncDataBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
        }
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

    @Override
    public void onDettach() {
        disposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}