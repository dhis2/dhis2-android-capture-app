package org.dhis2.usescases.settings;

import static org.dhis2.commons.Constants.DATA_NOW;
import static org.dhis2.commons.Constants.META_NOW;
import static org.dhis2.commons.matomo.Actions.SYNC_CONFIG;
import static org.dhis2.commons.matomo.Actions.SYNC_DATA;
import static org.dhis2.commons.matomo.Categories.SETTINGS;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_DATA_NOW;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_METADATA_NOW;

import androidx.lifecycle.MutableLiveData;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkInfo;

import com.google.common.annotations.VisibleForTesting;

import org.dhis2.R;
import org.dhis2.commons.Constants;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkerItem;
import org.dhis2.data.service.workManager.WorkerType;
import org.dhis2.ui.model.ButtonUiModel;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.reservedValue.ReservedValueActivity;
import org.dhis2.usescases.settings.models.ErrorModelMapper;
import org.dhis2.usescases.settings.models.ErrorViewModel;
import org.dhis2.usescases.settings.models.SMSSettingsViewModel;
import org.dhis2.usescases.settings.models.SettingsViewModel;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.settings.LimitScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;
import timber.log.Timber;


public class SyncManagerPresenter {

    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private final PreferenceProvider preferenceProvider;
    private final SettingsRepository settingsRepository;
    private final UserManager userManager;
    private final AnalyticsHelper analyticsHelper;
    private final ErrorModelMapper errorMapper;
    private CompositeDisposable compositeDisposable;
    private SyncManagerContracts.View view;
    private FlowableProcessor<Boolean> checkData;
    private GatewayValidator gatewayValidator;
    private WorkManagerController workManagerController;
    private MatomoAnalyticsController matomoAnalyticsController;
    private SMSSettingsViewModel smsSettingsViewModel;
    private ResourceManager resourceManager;
    public MutableLiveData<ButtonUiModel> syncDataButton = new MutableLiveData<>();

    public MutableLiveData<ButtonUiModel> syncMetaDataButton = new MutableLiveData<>();

    SyncManagerPresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            GatewayValidator gatewayValidator,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController,
            SettingsRepository settingsRepository,
            UserManager userManager,
            SyncManagerContracts.View view,
            AnalyticsHelper analyticsHelper,
            ErrorModelMapper errorMapper,
            MatomoAnalyticsController matomoAnalyticsController,
            ResourceManager resourceManager) {
        this.view = view;
        this.d2 = d2;
        this.settingsRepository = settingsRepository;
        this.userManager = userManager;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        this.gatewayValidator = gatewayValidator;
        this.workManagerController = workManagerController;
        this.analyticsHelper = analyticsHelper;
        this.errorMapper = errorMapper;
        this.matomoAnalyticsController = matomoAnalyticsController;
        this.resourceManager = resourceManager;
        checkData = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();
    }

    public void onItemClick(SettingItem settingsItem) {
        view.openItem(settingsItem);
    }

    public void init() {
        compositeDisposable.add(
                checkData.startWith(true)
                        .flatMapSingle(start ->
                                Single.zip(
                                        settingsRepository.metaSync(userManager),
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
                                    this.smsSettingsViewModel = settingsViewModel.getSmsSettingsViewModel();
                                },
                                Timber::e
                        ));

        syncDataButton.postValue(new ButtonUiModel(
                resourceManager.getString(R.string.SYNC_DATA).toUpperCase(),
                true,
                () -> {
                    syncData();
                    return Unit.INSTANCE;
                }
        ));

        syncMetaDataButton.postValue(new ButtonUiModel(
                resourceManager.getString(R.string.SYNC_META).toUpperCase(),
                true,
                () -> {
                    syncMeta();
                    return Unit.INSTANCE;
                }
        ));
    }

    public int getMetadataPeriodSetting() {
        return settingsRepository.metaSync(userManager)
                .blockingGet()
                .getMetadataSyncPeriod();
    }

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
        view.hideGatewayError();
        return true;
    }

    public void saveLimitScope(LimitScope limitScope) {
        String syncParam = "sync_limitScope_save";
        matomoAnalyticsController.trackEvent(SETTINGS, syncParam, CLICK);
        settingsRepository.saveLimitScope(limitScope);
        checkData.onNext(true);
    }

    public void saveEventMaxCount(Integer eventsNumber) {
        String syncParam = "sync_eventMaxCount_save";
        matomoAnalyticsController.trackEvent(SETTINGS, syncParam, CLICK);
        settingsRepository.saveEventsToDownload(eventsNumber);
        checkData.onNext(true);
    }

    public void saveTeiMaxCount(Integer teiNumber) {
        String syncParam = "sync_teiMaxCoung_save";
        matomoAnalyticsController.trackEvent(SETTINGS, syncParam, CLICK);
        settingsRepository.saveTeiToDownload(teiNumber);
        checkData.onNext(true);
    }

    public void saveReservedValues(Integer reservedValuesCount) {
        String syncParam = "sync_reservedValues_save";
        matomoAnalyticsController.trackEvent(SETTINGS, syncParam, CLICK);
        settingsRepository.saveReservedValuesToDownload(reservedValuesCount);
        checkData.onNext(true);
    }

    public void saveGatewayNumber(String gatewayNumber) {
        if (isGatewaySetAndValid(gatewayNumber)) {
            settingsRepository.saveGatewayNumber(gatewayNumber);
        }
    }

    public void saveSmsResultSender(String smsResultSender) {
        settingsRepository.saveSmsResultSender(smsResultSender);
    }

    public void saveSmsResponseTimeout(Integer smsResponseTimeout) {
        settingsRepository.saveSmsResponseTimeout(smsResponseTimeout);
    }

    public void saveWaitForSmsResponse(boolean shouldWait) {
        settingsRepository.saveWaitForSmsResponse(shouldWait);
    }

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

    public void resetFilters() {
        FilterManager.getInstance().clearAllFilters();
    }

    public void onWorkStatusesUpdate(WorkInfo.State workState, String workerTag) {
        if (workState != null) {
            switch (workState) {
                case ENQUEUED:
                case RUNNING:
                case BLOCKED:
                    if (workerTag.equals(META_NOW)) {
                        view.onMetadataSyncInProgress();
                    } else if (workerTag.equals(DATA_NOW)) {
                        view.onDataSyncInProgress();
                    }
                    break;
                case SUCCEEDED:
                case FAILED:
                case CANCELLED:
                default:
                    if (workerTag.equals(META_NOW)) {
                        view.onMetadataFinished();
                    } else if (workerTag.equals(DATA_NOW)) {
                        view.onDataFinished();
                    }
            }
        } else {
            if (workerTag.equals(META_NOW)) {
                view.onMetadataFinished();
            } else if (workerTag.equals(DATA_NOW)) {
                view.onDataFinished();
            }
        }
    }

    public void syncData(int seconds, String scheduleTag) {
        preferenceProvider.setValue(Constants.TIME_DATA, seconds);
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.DATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    public void syncMeta(int seconds, String scheduleTag) {
        matomoAnalyticsController.trackEvent(SETTINGS, SYNC_DATA, CLICK);
        preferenceProvider.setValue(Constants.TIME_META, seconds);
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.METADATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
        checkData();
    }

    public void syncData() {
        matomoAnalyticsController.trackEvent(SETTINGS, SYNC_CONFIG, CLICK);
        view.syncData();
        analyticsHelper.setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.DATA_NOW, WorkerType.DATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
        checkData();
    }

    public void syncMeta() {
        view.syncMeta();
        analyticsHelper.setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.META_NOW, WorkerType.METADATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
    }

    public void cancelPendingWork(String tag) {
        preferenceProvider.setValue(tag.equals(Constants.DATA) ? Constants.TIME_DATA : Constants.TIME_META, 0);
        workManagerController.cancelUniqueWork(tag);
        checkData();
    }

    public void dispose() {
        compositeDisposable.clear();
    }

    public void resetSyncParameters() {
        preferenceProvider.setValue(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        preferenceProvider.setValue(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        preferenceProvider.setValue(Constants.LIMIT_BY_ORG_UNIT, false);
        preferenceProvider.setValue(Constants.LIMIT_BY_PROGRAM, false);

        checkData.onNext(true);
    }

    public void wipeDb() {
        try {
            workManagerController.cancelAllWork();
            workManagerController.pruneWork();
            // clearing cache data

            DeleteCacheKt.deleteCache(view.getAbstracContext().getCacheDir());

            preferenceProvider.clear();

            d2.wipeModule().wipeEverything();
            d2.userModule().logOut().blockingAwait();

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            view.startActivity(LoginActivity.class, null, true, true, null);
        }
    }

    public void onDeleteLocalData() {
        view.deleteLocalData();
    }

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

    public void onReservedValues() {
        view.startActivity(ReservedValueActivity.class, null, false, false, null);
    }

    public void checkSyncErrors() {
        compositeDisposable.add(Single.fromCallable(() -> {
                    List<ErrorViewModel> errors = new ArrayList<>();
                    errors.addAll(errorMapper.mapD2Error(d2.maintenanceModule().d2Errors().blockingGet()));
                    errors.addAll(errorMapper.mapConflict(d2.importModule().trackerImportConflicts().blockingGet()));
                    errors.addAll(errorMapper.mapFKViolation(d2.maintenanceModule().foreignKeyViolations().blockingGet()));
                    return errors;
                })
                .map(errors -> {
                    Collections.sort(
                            errors,
                            (errorA, errorB) -> errorB.component1().compareTo(errorA.component1())
                    );
                    return errors;
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        errors -> view.showSyncErrors(errors),
                        Timber::e
                ));
    }

    public void checkData() {
        checkData.onNext(true);
    }

    public void checkGatewayAndTimeoutAreValid() {
        if (view.isGatewayValid() && view.isResultTimeoutValid()) {
            view.enabledSMSSwitchAndSender(smsSettingsViewModel);
        }
    }

    @VisibleForTesting
    public void setSmsSettingsViewModel(SMSSettingsViewModel settingsViewModel) {
        this.smsSettingsViewModel = settingsViewModel;
    }

    public void updateSyncDataButton(boolean canBeClicked) {
        syncDataButton.postValue(new ButtonUiModel(
                resourceManager.getString(R.string.SYNC_DATA).toUpperCase(),
                canBeClicked,
                () -> {
                    syncData();
                    return Unit.INSTANCE;
                }
        ));
    }

    public void updateSyncMetaDataButton(boolean canBeClicked) {
        syncMetaDataButton.postValue(
                new ButtonUiModel(
                        resourceManager.getString(R.string.SYNC_META).toUpperCase(),
                        canBeClicked,
                        () -> {
                            syncMeta();
                            return Unit.INSTANCE;
                        }
                )
        );
    }
}
