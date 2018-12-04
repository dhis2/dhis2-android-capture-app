package org.dhis2.usescases.sync;

import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.disposables.CompositeDisposable;

public class SyncPresenterImpl implements SyncContracts.Presenter {


    private SyncContracts.View view;
    private CompositeDisposable disposable;

    @Override
    public void init(SyncContracts.View view) {
        this.view = view;
        this.disposable = new CompositeDisposable();

    }

    @Override
    public void initMetaSync() {
        WorkManager.getInstance().cancelAllWorkByTag("Meta");
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, 1, TimeUnit.DAYS);
        syncDataBuilder.addTag("Data");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public void initDataSync() {
        WorkManager.getInstance().cancelAllWorkByTag("Data");
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, 1, TimeUnit.DAYS);
        syncDataBuilder.addTag("Data");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();

        WorkManager.getInstance().getStatusByIdLiveData(request.getId()).
                observe(view.getAbstractActivity(), workStatus -> {
                    view.updateView("Data", workStatus.getState());
                });
        WorkManager.getInstance().enqueue(request);

    }

    @Override
    public void initRVSync() {

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
