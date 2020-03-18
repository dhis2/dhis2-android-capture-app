package org.dhis2.usescases.settings;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;

import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkerItem;
import org.dhis2.data.service.workManager.WorkerType;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.reservedValue.ReservedValueActivity;
import org.dhis2.usescases.settings.models.SettingsViewModel;
import org.dhis2.utils.Constants;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.settings.LimitScope;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_DATA_NOW;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_METADATA_NOW;


public class SyncManagerPresenter implements SyncManagerContracts.Presenter {

    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final PreferenceProvider preferenceProvider;
    private final SettingsRepository settingsRepository;
    private final AnalyticsHelper analyticsHelper;
    private CompositeDisposable compositeDisposable;
    private SyncManagerContracts.View view;
    private FlowableProcessor<Boolean> checkData;
    private GatewayValidator gatewayValidator;
    private WorkManagerController workManagerController;

    SyncManagerPresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            GatewayValidator gatewayValidator,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController,
            SettingsRepository settingsRepository,
            SyncManagerContracts.View view,
            AnalyticsHelper analyticsHelper) {
        this.view = view;
        this.d2 = d2;
        this.settingsRepository = settingsRepository;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        this.gatewayValidator = gatewayValidator;
        this.workManagerController = workManagerController;
        this.analyticsHelper = analyticsHelper;
        checkData = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onItemClick(SettingItem settingsItem) {
        view.openItem(settingsItem);
    }

    @Override
    public void init() {
        compositeDisposable.add(
                checkData.startWith(true)
                        .flatMapSingle(start ->
                                Single.zip(
                                        settingsRepository.metaSync(),
                                        settingsRepository.dataSync(),
                                        settingsRepository.syncParameters(),
                                        settingsRepository.reservedValues(),
                                        settingsRepository.sms(),
                                        SettingsViewModel::new
                                ))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                settingsViewModel -> {
                                    view.setMetadataSettings(settingsViewModel.getMetadataSettingsViewModel());
                                    view.setDataSettings(settingsViewModel.getDataSettingsViewModel());
                                    view.setParameterSettings(settingsViewModel.getSyncParametersViewModel());
                                    view.setReservedValuesSettings(settingsViewModel.getReservedValueSettingsViewModel());
                                    view.setSMSSettings(settingsViewModel.getSmsSettingsViewModel());
                                },
                                Timber::e
                        ));
    }

    @Override
    public int getMetadataPeriodSetting() {
        return settingsRepository.metaSync()
                .blockingGet()
                .getMetadataSyncPeriod();
    }

    @Override
    public int getDataPeriodSetting() {
        return settingsRepository.dataSync()
                .blockingGet()
                .getDataSyncPeriod();
    }

    public void validateGatewayObservable(String gateway) {
        if (plusIsMissingOrIsTooLong(gateway)) {
            view.showInvalidGatewayError();
        } else if (gateway.isEmpty()) {
            view.requestNoEmptySMSGateway();
        } else if (isValidGateway(gateway)) {
            view.hideGatewayError();
        }
    }

    private boolean isValidGateway(String gateway) {
        return gatewayValidator.validate(gateway) ||
                (gateway.startsWith("+") && gateway.length() == 1);
    }

    private boolean plusIsMissingOrIsTooLong(String gateway) {
        return (!gateway.startsWith("+") && gateway.length() == 1) ||
                (gateway.length() >= GatewayValidator.Companion.getMax_size());
    }

    public boolean isGatewaySetAndValid(String gateway) {
        if (gateway.isEmpty()) {
            view.requestNoEmptySMSGateway();
            return false;
        } else if (!gatewayValidator.validate(gateway)) {
            view.showInvalidGatewayError();
            return false;
        }
        return true;
    }

    @Override
    public void saveLimitScope(LimitScope limitScope) {
        settingsRepository.saveLimitScope(limitScope);
        checkData.onNext(true);
    }

    @Override
    public void saveEventMaxCount(Integer eventsNumber) {
        settingsRepository.saveEventsToDownload(eventsNumber);
        checkData.onNext(true);
    }

    @Override
    public void saveTeiMaxCount(Integer teiNumber) {
        settingsRepository.saveTeiToDownload(teiNumber);
        checkData.onNext(true);
    }

    @Override
    public void saveReservedValues(Integer reservedValuesCount) {
        settingsRepository.saveReservedValuesToDownload(reservedValuesCount);
        checkData.onNext(true);
    }

    @Override
    public void saveGatewayNumber(String gatewayNumber) {
        if (isGatewaySetAndValid(gatewayNumber)) {
            settingsRepository.saveGatewayNumber(gatewayNumber);
        }
    }

    @Override
    public void saveSmsResultSender(String smsResultSender) {
        settingsRepository.saveSmsResultSender(smsResultSender);
    }

    @Override
    public void saveSmsResponseTimeout(Integer smsResponseTimeout) {
        settingsRepository.saveSmsResponseTimeout(smsResponseTimeout);
    }

    @Override
    public void saveWaitForSmsResponse(boolean shouldWait) {
        settingsRepository.saveWaitForSmsResponse(shouldWait);
    }

    @Override
    public void enableSmsModule(boolean enableSms) {
        if (enableSms) {
            view.displaySMSRefreshingData();
        }
        compositeDisposable.add(
                settingsRepository.enableSmsModule(enableSms)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                () -> view.displaySMSEnabled(enableSms),
                                error -> {
                                    Timber.e(error);
                                    view.displaySmsEnableError();
                                }
                        )
        );
    }

    @Override
    public void syncData(int seconds, String scheduleTag) {
        preferenceProvider.setValue(Constants.TIME_DATA, seconds);
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.DATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        preferenceProvider.setValue(Constants.TIME_META, seconds);
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.METADATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    @Override
    public void syncData() {
        view.syncData();
        analyticsHelper.setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.DATA_NOW, WorkerType.DATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
        checkData();
    }

    @Override
    public void syncMeta() {
        view.syncMeta();
        analyticsHelper.setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.META_NOW, WorkerType.METADATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
    }

    @Override
    public void cancelPendingWork(String tag) {
        preferenceProvider.setValue(tag.equals(Constants.DATA) ? Constants.TIME_DATA : Constants.TIME_META, 0);
        workManagerController.cancelUniqueWork(tag);
        checkData();
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    @Override
    public void resetSyncParameters() {
        preferenceProvider.setValue(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        preferenceProvider.setValue(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        preferenceProvider.setValue(Constants.LIMIT_BY_ORG_UNIT, false);
        preferenceProvider.setValue(Constants.LIMIT_BY_PROGRAM, false);

        checkData.onNext(true);
    }

    @Override
    public void onWipeData() {
        view.wipeDatabase();
    }

    @Override
    public void wipeDb() {
        try {
            workManagerController.cancelAllWork();
            workManagerController.pruneWork();
            d2.userModule().logOut().blockingAwait();
            d2.wipeModule().wipeEverything();
            // clearing cache data
            deleteDir(view.getAbstracContext().getCacheDir());

            preferenceProvider.clear();

            view.startActivity(LoginActivity.class, null, true, true, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onDeleteLocalData() {
        view.deleteLocalData();
    }

    @Override
    public void deleteLocalData() {
        boolean error = false;
        try {
            d2.wipeModule().wipeData();
        } catch (D2Error e) {
            Timber.e(e);
            error = true;
        }

        view.showLocalDataDeleted(error);
    }

    @Override
    public void onReservedValues() {
        view.startActivity(ReservedValueActivity.class, null, false, false, null);
    }

    @Override
    public void checkSyncErrors() {
        view.showSyncErrors(d2.maintenanceModule().d2Errors().blockingGet());
    }

    @Override
    public void checkData() {
        checkData.onNext(true);
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
