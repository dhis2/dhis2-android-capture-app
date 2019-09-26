package org.dhis2.usescases.sync;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.R;
import org.dhis2.data.service.ReservedValuesWorker;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncInitWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.settings.SystemSetting;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SyncPresenter implements SyncContracts.Presenter {

    private final D2 d2;
    private SyncContracts.View view;

    private CompositeDisposable disposable;


    SyncPresenter(D2 d2) {
        this.d2 = d2;
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

        //METADATA
        OneTimeWorkRequest.Builder initMetaBuilder = new OneTimeWorkRequest.Builder(SyncInitWorker.class);
        initMetaBuilder.addTag(Constants.INIT_META);
        initMetaBuilder.setInitialDelay(metaTime, TimeUnit.SECONDS);
        initMetaBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        Data dataMeta = new Data.Builder()
                .putBoolean(SyncInitWorker.INIT_META, metaTime != 0)
                .putBoolean(SyncInitWorker.INIT_DATA, false)
                .build();
        initMetaBuilder.setInputData(dataMeta);
        WorkManager.getInstance(view.getContext().getApplicationContext())
                .enqueueUniqueWork(Constants.INIT_META, ExistingWorkPolicy.REPLACE, initMetaBuilder.build());

        //DATA
        OneTimeWorkRequest.Builder initDataBuilder = new OneTimeWorkRequest.Builder(SyncInitWorker.class);
        initDataBuilder.addTag(Constants.INIT_DATA);
        initDataBuilder.setInitialDelay(dataTime, TimeUnit.SECONDS);
        initDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        Data data = new Data.Builder()
                .putBoolean(SyncInitWorker.INIT_DATA, false)
                .putBoolean(SyncInitWorker.INIT_DATA, dataTime != 0)
                .build();
        initDataBuilder.setInputData(data);
        WorkManager.getInstance(view.getContext().getApplicationContext())
                .enqueueUniqueWork(Constants.INIT_DATA, ExistingWorkPolicy.REPLACE, initDataBuilder.build());
    }

    @Override
    public void getTheme() {
        disposable.add(
                d2.systemSettingModule().systemSetting.get()
                        .map(systemSettings -> {
                            String style = "";
                            String flag = "";
                            for (SystemSetting setting : systemSettings) {
                                if (setting.key() == SystemSetting.SystemSettingKey.STYLE)
                                    style = setting.value();
                                else
                                    flag = setting.value();
                            }
                            if (style.contains("green"))
                                return Pair.create(flag, R.style.GreenTheme);
                            if (style.contains("india"))
                                return Pair.create(flag, R.style.OrangeTheme);
                            if (style.contains("myanmar"))
                                return Pair.create(flag, R.style.RedTheme);
                            else
                                return Pair.create(flag, R.style.AppTheme);
                        })
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