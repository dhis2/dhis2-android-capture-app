package com.dhis2.usescases.splash;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.server.UserManager;
import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.main.MainActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SplashPresenter implements SplashContracts.Presenter {

    private SplashContracts.View view;
    private UserManager userManager;
    @NonNull
    private final CompositeDisposable compositeDisposable;

    SplashPresenter(@Nullable UserManager userManager) {
        this.view = view;
        this.userManager = userManager;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void destroy() {
        compositeDisposable.clear();
    }

    @Override
    public void init(SplashContracts.View view) {
        this.view = view;
    }

    @Override
    public void isUserLoggedIn() {
        if (userManager == null) {
            navigateToLoginView();
            return;
        }

        compositeDisposable.add(userManager.isUserLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((isUserLoggedIn) -> {
                    if (isUserLoggedIn) {
                        navigateToHomeView();
                    } else {
                        navigateToLoginView();
                    }
                }, Timber::e));
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