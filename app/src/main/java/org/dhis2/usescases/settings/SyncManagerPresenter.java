package org.dhis2.usescases.settings;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_DATA_NOW;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_METADATA_NOW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkerItem;
import org.dhis2.data.service.workManager.WorkerType;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.reservedValue.ReservedValueActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;


public class SyncManagerPresenter implements SyncManagerContracts.Presenter {

    private final D2 d2;

    private final SchedulerProvider schedulerProvider;

    private final PreferenceProvider preferenceProvider;

    private WorkManagerController workManagerController;

    private CompositeDisposable compositeDisposable;

    private SyncManagerContracts.View view;

    private FlowableProcessor<Boolean> checkData;

    private SharedPreferences prefs;

    SyncManagerPresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController) {
        this.d2 = d2;
        this.schedulerProvider = schedulerProvider;
        this.preferenceProvider = preferenceProvider;
        this.workManagerController = workManagerController;
        checkData = PublishProcessor.create();
    }

    @Override
    public void onItemClick(int settingsItem) {
        view.openItem(settingsItem);
    }

    @Override
    public void init(SyncManagerContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
        this.prefs = view.getAbstracContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        compositeDisposable.add(checkData.startWith(true).map(start -> {
            int teiCount = d2.trackedEntityModule().trackedEntityInstances().byState().neq(State.RELATIONSHIP)
                    .blockingCount();
            int eventCount = d2.eventModule().events().get().toObservable().map(events -> {
                List<Event> eventsToCount = new ArrayList<>();
                for (Event event : events) {
                    if (event.enrollment() == null)
                        eventsToCount.add(event);
                }
                return eventsToCount.size();
            }).blockingLast();
            return Pair.create(teiCount, eventCount);
        }).subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(view.setSyncData(),
                Timber::e));

        ConfigCase smsConfig = d2.smsModule().configCase();
        compositeDisposable.add(smsConfig.getSmsModuleConfig().subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui()).subscribeWith(new DisposableSingleObserver<ConfigCase.SmsConfig>() {
                    @Override
                    public void onSuccess(ConfigCase.SmsConfig c) {
                        view.showSmsSettings(c.isModuleEnabled(), c.getGateway(), c.isWaitingForResult(),
                                c.getResultSender(), c.getResultWaitingTimeout());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                }));
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
        workManagerController.cancelUniqueWork(scheduleTag);
        WorkerItem workerItem = new WorkerItem(scheduleTag, WorkerType.DATA, (long) seconds, null, null, ExistingPeriodicWorkPolicy.REPLACE);
        workManagerController.enqueuePeriodicWork(workerItem);
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
    }

    /**
     * This method allows you to run a DATA sync work.
     */
    @Override
    public void syncData() {
        view.analyticsHelper().setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW);
        WorkerItem workerItem = new WorkerItem(Constants.DATA_NOW, WorkerType.DATA, null, null, ExistingWorkPolicy.KEEP, null);
        workManagerController.syncDataForWorker(workerItem);
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
        workManagerController.cancelUniqueWork(tag);
    }

    @Override
    public void smsNumberSet(String number) {
        compositeDisposable.add(
                d2.smsModule().configCase().setGatewayNumber(number).subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("SMS gateway set to %s", number);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.requestNoEmptySMSGateway();
                    }
                }));
    }

    @Override
    public void smsSwitch(boolean isChecked) {

        if(d2.smsModule().configCase().getSmsModuleConfig().blockingGet().isModuleEnabled() != isChecked) {

            Completable completable;
            if (isChecked) {
                view.displaySMSRefreshingData();
                completable = d2.smsModule().configCase().setModuleEnabled(true)
                        .andThen(d2.smsModule().configCase().refreshMetadataIds());
            } else {
                completable = d2.smsModule().configCase().setModuleEnabled(false);
            }

            compositeDisposable
                    .add(completable.subscribeOn(schedulerProvider.io()).subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            view.displaySMSEnabled(isChecked);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e);
                        }
                    }));
        }
    }

    @Override
    public void smsResponseSenderSet(String number) {
        compositeDisposable.add(d2.smsModule().configCase().setConfirmationSenderNumber(number)
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("SMS response sender set to %s", number);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                }));
    }

    @Override
    public void smsWaitForResponse(boolean waitForResponse) {
        compositeDisposable.add(d2.smsModule().configCase().setWaitingForResultEnabled(waitForResponse)
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("SMS waiting for response: %b", waitForResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                }));
    }

    @Override
    public void smsWaitForResponseTimeout(int timeout) {
        compositeDisposable.add(d2.smsModule().configCase().setWaitingResultTimeout(timeout)
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("SMS waiting for response timeout: %d", timeout);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }
                }));
    }

    @Override
    public boolean dataHasErrors() {
        return !d2.eventModule().events().byState().in(State.ERROR).blockingGet().isEmpty()
                || !d2.trackedEntityModule().trackedEntityInstances().byState().in(State.ERROR).blockingGet().isEmpty();
    }

    @Override
    public boolean dataHasWarnings() {
        return !d2.eventModule().events().byState().in(State.WARNING).blockingGet().isEmpty()
                || !d2.trackedEntityModule().trackedEntityInstances().byState().in(State.WARNING).blockingGet().isEmpty();
    }

    @Override
    public void disponse() {
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
        view.showSyncErrors(d2.importModule().trackerImportConflicts().blockingGet());
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
