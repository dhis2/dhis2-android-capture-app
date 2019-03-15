package org.dhis2.usescases.splash;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.sync.SyncActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.SyncUtils;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public class SplashPresenter implements SplashContracts.Presenter {

    private final SplashRepository splashRespository;
    private SplashContracts.View view;
    private UserManager userManager;
    @NonNull
    private final CompositeDisposable compositeDisposable;

    SplashPresenter(@Nullable UserManager userManager, SplashRepository splashRepository) {
        this.userManager = userManager;
        this.compositeDisposable = new CompositeDisposable();
        this.splashRespository = splashRepository;
    }

    @Override
    public void destroy() {
        compositeDisposable.clear();
    }

    @Override
    public void init(SplashContracts.View view) {
        this.view = view;

        compositeDisposable.add(splashRespository.getIconForFlag()
                .delay(2, TimeUnit.SECONDS, Schedulers.io())
                .map(flagName -> {
                    if (!isEmpty(flagName)) {
                        Resources resources = view.getAbstracContext().getResources();
                        return resources.getIdentifier(flagName, "drawable", view.getAbstracContext().getPackageName());
                    } else
                        return -1;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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

        if (SyncUtils.isSyncRunning()) {

            view.startActivity(SyncActivity.class, null, true, true, null);

        } else {

            compositeDisposable.add(userManager.isUserLoggedIn()
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