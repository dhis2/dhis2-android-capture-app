package org.dhis2.usescases.sync;

import org.dhis2.R;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkerItem;
import org.dhis2.data.service.workManager.WorkerType;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.settings.SystemSetting;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class SyncPresenter implements SyncContracts.Presenter {

    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private WorkManagerController workManagerController;
    private SyncContracts.View view;

    private CompositeDisposable disposable;

    SyncPresenter(D2 d2, SchedulerProvider schedulerProvider, WorkManagerController workManagerController) {
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.workManagerController = workManagerController;
    }

    @Override
    public void init(SyncContracts.View view) {
        this.view = view;
        this.disposable = new CompositeDisposable();

    }

    @Override
    public void sync() {
        workManagerController
                .syncDataForWorkers(Constants.META_NOW, Constants.DATA_NOW, Constants.INITIAL_SYNC);
    }

    @Override
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
                            if (style.contains("green"))
                                return Pair.create(flag, R.style.GreenTheme);
                            if (style.contains("india"))
                                return Pair.create(flag, R.style.OrangeTheme);
                            if (style.contains("myanmar"))
                                return Pair.create(flag, R.style.RedTheme);
                            else
                                return Pair.create(flag, R.style.AppTheme);
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(flagTheme -> {
                                    view.saveFlag(flagTheme.val0());
                                    view.saveTheme(flagTheme.val1());
                                }, Timber::e
                        ));
    }

    @Override
    public void syncReservedValues() {
        WorkerItem workerItem = new WorkerItem(Constants.RESERVED, WorkerType.RESERVED, null, null, null, null);
        workManagerController.cancelAllWorkByTag(workerItem.getWorkerName());
        workManagerController.syncDataForWorker(workerItem);
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