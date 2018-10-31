package org.dhis2.usescases.syncManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.D2CallException;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.usescases.syncManager.SyncManagerFragment.TAG_DATA_NOW;
import static org.dhis2.usescases.syncManager.SyncManagerFragment.TAG_META_NOW;

/**
 * QUADRAM. Created by lmartin on 21/03/2018.
 */

public class SyncManagerPresenter implements SyncManagerContracts.Presenter {

    private final D2 d2;

    private MetadataRepository metadataRepository;
    private FirebaseJobDispatcher dispatcher;
    private CompositeDisposable compositeDisposable;
    private SyncManagerContracts.View view;
    private FlowableProcessor<Boolean> checkData;

    SyncManagerPresenter(MetadataRepository metadataRepository, FirebaseJobDispatcher dispatcher, D2 d2) {
        this.metadataRepository = metadataRepository;
        this.dispatcher = dispatcher;
        this.d2 = d2;
        checkData = PublishProcessor.create();
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

    @Override
    public void syncData(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public void syncData() {

        WorkManager.getInstance().cancelAllWorkByTag(TAG_DATA_NOW);
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncDataWorker.class);
        syncDataBuilder.addTag(TAG_DATA_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
    }

    @Override
    public void syncMeta() {
        WorkManager.getInstance().cancelAllWorkByTag(TAG_META_NOW);
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(SyncMetadataWorker.class);
        syncDataBuilder.addTag(TAG_META_NOW);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);
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

        editor.apply();

        compositeDisposable.add(
                Observable.just(Pair.create(Constants.EVENT_MAX_DEFAULT, Constants.TEI_MAX_DEFAULT))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.setSyncData(),
                                Timber::d
                        )
        );

    }

    @Override
    public void onWipeData() {

        view.wipeDatabase();

    }

    @Override
    public void wipeDb() {
        try {
            dispatcher.cancelAll();
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
    public void deleteLocalData() {
        try {
            d2.wipeModule().wipeData();
        } catch (D2CallException e) {
            Timber.e(e);
        }
    }

    @Override
    public void checkSyncErrors() {
        compositeDisposable.add(
                metadataRepository.getSyncErrors()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showSyncErrors(data),
                                Timber::e

                        )
        );
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
