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
    public void sync() {
        OneTimeWorkRequest.Builder syncMetaBuilder = new OneTimeWorkRequest.Builder(SyncMetadataWorker.class);
        syncMetaBuilder.addTag(Constants.META_NOW);
        syncMetaBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest metaRequest = syncMetaBuilder.build();

        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncDataWorker.class);
        syncDataBuilder.addTag(Constants.DATA_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest dataRequest = syncDataBuilder.build();

        WorkManager.getInstance(view.getContext().getApplicationContext())
                .beginUniqueWork(Constants.INITIAL_SYNC, ExistingWorkPolicy.KEEP, metaRequest)
                .then(dataRequest)
                .enqueue();

    }

    @Override
    public void scheduleSync(int metaTime, int dataTime) {
        if (metaTime != 0) {
            PeriodicWorkRequest.Builder metaBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, metaTime, TimeUnit.SECONDS);
            metaBuilder.addTag(Constants.META);
            metaBuilder.setInitialDelay(metaTime, TimeUnit.SECONDS); //TODO: CAN BE SET TO A SPECIFIC TIME
            metaBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            PeriodicWorkRequest metaRequest = metaBuilder.build();
            WorkManager.getInstance(view.getContext().getApplicationContext()).enqueueUniquePeriodicWork(Constants.META, ExistingPeriodicWorkPolicy.REPLACE, metaRequest);
        }

        if (dataTime != 0) {
            PeriodicWorkRequest.Builder dataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, dataTime, TimeUnit.SECONDS);
            dataBuilder.addTag(Constants.DATA);
            dataBuilder.setInitialDelay(dataTime, TimeUnit.SECONDS);//TODO: CAN BE SET TO A SPECIFIC TIME
            dataBuilder.setConstraints(new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build());
            PeriodicWorkRequest dataRequest = dataBuilder.build();
            WorkManager.getInstance(view.getContext().getApplicationContext()).enqueueUniquePeriodicWork(Constants.DATA, ExistingPeriodicWorkPolicy.REPLACE, dataRequest);
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

        WorkManager.getInstance(view.getContext().getApplicationContext()).cancelAllWorkByTag("TAG_RV");
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(ReservedValuesWorker.class);
        syncDataBuilder.addTag("TAG_RV");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(view.getContext().getApplicationContext()).enqueue(request);

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