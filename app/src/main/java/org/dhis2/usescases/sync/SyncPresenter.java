package org.dhis2.usescases.sync;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.service.ReservedValuesWorker;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.settings.SystemSetting;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class SyncPresenter {

    private SyncView view;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private WorkManager workManager;

    public CompositeDisposable disposable;

    SyncPresenter(SyncView view, D2 d2, SchedulerProvider schedulerProvider, WorkManager workManager) {
        this.view = view;
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.workManager = workManager;
        this.disposable = new CompositeDisposable();
    }

    public void sync() {
        //META WORK REQUEST
        OneTimeWorkRequest.Builder syncMetaBuilder = new OneTimeWorkRequest.Builder(SyncMetadataWorker.class);
        syncMetaBuilder.addTag(Constants.META_NOW);
        syncMetaBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest metaRequest = syncMetaBuilder.build();

        //DATA WORK REQUEST
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncDataWorker.class);
        syncDataBuilder.addTag(Constants.DATA_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest dataRequest = syncDataBuilder.build();

        //FULL REQUEST
        workManager
                .beginUniqueWork(Constants.INITIAL_SYNC, ExistingWorkPolicy.KEEP, metaRequest)
                .then(dataRequest)
                .enqueue();

    }

    public void getTheme() {
        disposable.add(
                d2.systemSettingModule().systemSetting().get()
                        .map(systemSettings -> {
                            String style = "";
                            String flag = "";
                            for (SystemSetting setting : systemSettings) {
                                if (setting.key() == SystemSetting.SystemSettingKey.STYLE)
                                    style = setting.value();
                                else
                                    flag = setting.value();
                            }
                            return Pair.create(flag, style);
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(flagTheme -> {
                                    view.saveFlag(flagTheme.val0());
                                    view.saveTheme(flagTheme.val1());
                                }, Timber::e
                        ));
    }

    public void syncReservedValues() {

        workManager.cancelAllWorkByTag("TAG_RV");
        OneTimeWorkRequest.Builder syncDataBuilder =
                new OneTimeWorkRequest.Builder(ReservedValuesWorker.class);
        syncDataBuilder.addTag("TAG_RV");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        workManager.enqueue(request);
    }

    public void onDettach() {
        disposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}