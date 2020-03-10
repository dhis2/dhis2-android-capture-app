package org.dhis2.usescases.settings;

import android.content.Context;
import android.content.SharedPreferences;

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
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.settings.LimitScope;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
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
    private CompositeDisposable compositeDisposable;
    private SyncManagerContracts.View view;
    private FlowableProcessor<Boolean> checkData;
    private SharedPreferences prefs;
    private GatewayValidator gatewayValidator;
    private WorkManagerController workManagerController;

    SyncManagerPresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            GatewayValidator gatewayValidator,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController,
            SettingsRepository settingsRepository) {
        this.d2 = d2;
        this.settingsRepository = settingsRepository;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        this.gatewayValidator = gatewayValidator;
        this.workManagerController = workManagerController;
        checkData = PublishProcessor.create();
    }

    @Override
    public void onItemClick(SettingItem settingsItem) {
        view.openItem(settingsItem);
    }

    @Override
    public void init(SyncManagerContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
        this.prefs = view.getAbstracContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);

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

    /*    compositeDisposable.add(
                view.listenToMaxEventsChanges()
                        .distinctUntilChanged()
                        .debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .map(CharSequence::toString)
                        .map(dataToSave -> dataToSave.isEmpty() ? 0 : Integer.valueOf(dataToSave))
                        .toFlowable(BackpressureStrategy.LATEST)
                        .flatMap(
                                settingsRepository::saveEventsToDownload)
                        .observeOn(schedulerProvider.io())
                        .subscribe(
                                data -> checkData.onNext(data),
                                Timber::e
                        ));

        compositeDisposable.add(
                view.listenToMaxTeiChanges()
                        .distinctUntilChanged()
                        .debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .map(CharSequence::toString)
                        .map(dataToSave -> dataToSave.isEmpty() ? 0 : Integer.valueOf(dataToSave))
                        .toFlowable(BackpressureStrategy.LATEST)
                        .flatMap(
                                settingsRepository::saveTeiToDownload)
                        .observeOn(schedulerProvider.io())
                        .subscribe(
                                data -> checkData.onNext(data),
                                Timber::d
                        ));

        compositeDisposable.add(
                InitialValueObservable.combineLatest(
                        view.listenToGatewayChanges().skipInitialValue().map(CharSequence::toString)
                                .distinctUntilChanged(),
                        view.listenToSmsChanges()
                                .distinctUntilChanged(),
                        Pair::create
                ).debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                gateWayAndEnabled -> {
                                    String gateway = gateWayAndEnabled.val0();
                                    boolean smsEnabled = gateWayAndEnabled.val1();
                                    if (!smsEnabled) {
                                        smsSwitch(false);
                                    } else if (isGatewaySetAndValid(gateway)) {
                                        smsSwitch(true);
                                    } else {
                                        validateGatewayObservable(gateway);
                                    }
                                }
                                ,
                                Timber::d
                        ));

        compositeDisposable.add(
                view.listenToSmsResponseChanges()
                        .distinctUntilChanged()
                        .debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                this::smsWaitForResponse,
                                Timber::d
                        ));
        compositeDisposable.add(
                view.listenToSmsResultSenderChanges()
                        .distinctUntilChanged()
                        .debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                number -> smsResponseSenderSet(number.toString()),
                                Timber::d
                        ));

        compositeDisposable.add(
                view.listenToSmsTimeoutChanges()
                        .distinctUntilChanged()
                        .debounce(1000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .map(CharSequence::toString)
                        .map(Integer::valueOf)
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                this::smsWaitForResponseTimeout,
                                Timber::d
                        ));*/
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
            checkData();
        }
    }

    @Override
    public void saveSmsResultSender(String smsResultSender) {
        settingsRepository.saveSmsResultSender(smsResultSender);
        checkData();
    }

    @Override
    public void saveSmsResponseTimeout(Integer smsResponseTimeout) {
        settingsRepository.saveSmsResponseTimeout(smsResponseTimeout);
        checkData();
    }

    @Override
    public void saveWaitForSmsResponse(boolean shouldWait) {
        settingsRepository.saveWaitForSmsResponse(shouldWait);
        checkData();
    }

    @Override
    public void enableSmsModule(boolean enableSms) {
        if (enableSms) {
            view.displaySMSRefreshingData();
        }
        settingsRepository.enableSmsModule(enableSms, () -> view.displaySMSEnabled(enableSms));
    }

    /**
     * This method allows you to create a new periodic DATA sync work with an
     * interval defined by {@code seconds}. All scheduled works will be cancelled in
     * order to reschedule a new one.
     *
     * @param seconds     period interval in seconds
     * @param scheduleTag Name of the periodic work (DATA)
     */
    @Override
    public void syncData(int seconds, String scheduleTag) {
        prefs.edit().putInt(Constants.TIME_DATA, seconds).apply();
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.DATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    /**
     * This method allows you to create a new periodic METADATA sync work with an
     * interval defined by {@code seconds}. All scheduled works will be cancelled in
     * order to reschedule a new one.
     *
     * @param seconds     period interval in seconds
     * @param scheduleTag Name of the periodic work (META)
     */
    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.METADATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    /**
     * This method allows you to run a DATA sync work.
     */
    @Override
    public void syncData() {
        view.syncData();
        view.analyticsHelper().setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.DATA_NOW, WorkerType.DATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
        checkData();
    }

    /**
     * This method allows you to run a METADATA sync work.
     */
    @Override
    public void syncMeta() {
        view.syncMeta();
        view.analyticsHelper().setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.META_NOW, WorkerType.METADATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
    }

    @Override
    public void cancelPendingWork(String tag) {
        prefs.edit().putInt(tag.equals(Constants.DATA) ? Constants.TIME_DATA : Constants.TIME_META, 0).apply();
        workManagerController.cancelUniqueWork(tag);
        checkData();
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    @Override
    public void resetSyncParameters() {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        editor.putInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        editor.putBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        editor.putBoolean(Constants.LIMIT_BY_PROGRAM, false);

        editor.apply();

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
