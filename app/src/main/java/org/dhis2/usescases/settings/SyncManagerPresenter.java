package org.dhis2.usescases.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.reservedValue.ReservedValueActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.maintenance.D2Error;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by lmartin on 21/03/2018.
 */

public class SyncManagerPresenter implements SyncManagerContracts.Presenter {

    private final D2 d2;

    private MetadataRepository metadataRepository;
    private CompositeDisposable compositeDisposable;
    private SyncManagerContracts.View view;
    private FlowableProcessor<Boolean> checkData;

    SyncManagerPresenter(MetadataRepository metadataRepository, D2 d2) {
        this.metadataRepository = metadataRepository;
        this.d2 = d2;
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

        compositeDisposable.add(
                checkData
                        .startWith(true)
                        .flatMap(start ->
                                metadataRepository.getDownloadedData())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setSyncData(),
                                Timber::e
                        )
        );
    }

    /**
     * This method allows you to create a new periodic DATA sync work with an interval defined by
     * {@code seconds}.
     * All scheduled works will be cancelled in order to reschedule a new one.
     *
     * @param seconds     period interval in seconds
     * @param scheduleTag Name of the periodic work (DATA)
     */
    @Override
    public void syncData(int seconds, String scheduleTag) {
        WorkManager.getInstance(view.getContext().getApplicationContext()).cancelUniqueWork(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(view.getContext().getApplicationContext()).enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    /**
     * This method allows you to create a new periodic METADATA sync work with an interval defined by
     * {@code seconds}.
     * All scheduled works will be cancelled in order to reschedule a new one.
     *
     * @param seconds     period interval in seconds
     * @param scheduleTag Name of the periodic work (META)
     */
    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        WorkManager.getInstance(view.getContext().getApplicationContext()).cancelUniqueWork(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(view.getContext().getApplicationContext()).enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    /**
     * This method allows you to run a DATA sync work.
     */
    @Override
    public void syncData() {
        view.syncData();
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncDataWorker.class);
        syncDataBuilder.addTag(Constants.DATA_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(view.getContext().getApplicationContext()).beginUniqueWork(Constants.DATA_NOW, ExistingWorkPolicy.REPLACE, request).enqueue();
    }

    /**
     * This method allows you to run a METADATA sync work.
     */
    @Override
    public void syncMeta() {
        view.syncMeta();
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncMetadataWorker.class);
        syncDataBuilder.addTag(Constants.META_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance(view.getContext().getApplicationContext()).beginUniqueWork(Constants.META_NOW, ExistingWorkPolicy.REPLACE, request).enqueue();
    }


    @Override
    public void cancelPendingWork(String tag) {
        WorkManager.getInstance(view.getContext().getApplicationContext()).cancelUniqueWork(tag);
    }

    @Override
    public boolean dataHasErrors() {
        return !d2.eventModule().events.byState().in(State.ERROR).get().isEmpty() || !d2.trackedEntityModule().trackedEntityInstances.byState().in(State.ERROR).get().isEmpty();
    }

    @Override
    public boolean dataHasWarnings() {
        return !d2.eventModule().events.byState().in(State.WARNING).get().isEmpty() || !d2.trackedEntityModule().trackedEntityInstances.byState().in(State.WARNING).get().isEmpty();
    }

    @Override
    public void disponse() {
        compositeDisposable.clear();
    }

    @Override
    public void resetSyncParameters() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
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
            WorkManager.getInstance(view.getContext().getApplicationContext()).cancelAllWork();
            WorkManager.getInstance(view.getContext().getApplicationContext()).pruneWork();
            d2.wipeModule().wipeEverything();
            // clearing cache data
            deleteDir(view.getAbstracContext().getCacheDir());

            view.getAbstracContext().getSharedPreferences().edit().clear().apply();

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
        view.showSyncErrors(metadataRepository.getSyncErrors());
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
