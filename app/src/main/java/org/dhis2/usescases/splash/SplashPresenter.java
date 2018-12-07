package org.dhis2.usescases.splash;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.Bindings.Bindings;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.synchronization.SynchronizationActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.SyncUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SplashPresenter implements SplashContracts.Presenter {

    private final SplashRepository splashRespository;
    private SplashContracts.View view;
    private UserManager userManager;
    @NonNull
    private final CompositeDisposable compositeDisposable;

    SplashPresenter(@Nullable UserManager userManager, MetadataRepository metadataRepository, SplashRepository splashRepository) {
        this.userManager = userManager;
        this.compositeDisposable = new CompositeDisposable();
        this.splashRespository = splashRepository;
        Bindings.setMetadataRepository(metadataRepository);
    }

    @Override
    public void destroy() {
        compositeDisposable.clear();
    }

    @Override
    public void init(SplashContracts.View view) {
        this.view = view;

        compositeDisposable.add(splashRespository.getIconForFlag()
                .map(flagName -> {
                    Resources resources = view.getAbstracContext().getResources();
                    return resources.getIdentifier(flagName, "drawable", view.getAbstracContext().getPackageName());
                })
                .subscribe(
                        view.renderFlag(),
                        Timber::d
                )
        );
    }

    @Override
    public void isUserLoggedIn() {
        if (userManager == null) {
            navigateToLoginView();
            return;
        }

        if (SyncUtils.isSyncRunning()){

            view.startActivity(SynchronizationActivity.class, null, true, true, null);

        }else {

            compositeDisposable.add(
                    splashRespository.checkExpiredEvents()
                            .subscribeOn(Schedulers.computation())
                            .flatMap(data -> userManager.isUserLoggedIn())
                            .delay(2000, TimeUnit.MILLISECONDS, Schedulers.io())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(isUserLoggedIn -> {
                                SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                        Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                                if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                                    navigateToHomeView();
                                } else {
                                    navigateToLoginView();
                                }
                            }, Timber::e));
        }
    }

    @Override
    public void navigateToLoginView() {
        view.startActivity(LoginActivity.class, null, true, true, null);
    }

    @Override
    public void navigateToHomeView() {
        view.startActivity(MainActivity.class, null, true, true, null);
    }

}