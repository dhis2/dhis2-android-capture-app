package com.dhis2.usescases.splash;

import android.support.annotation.NonNull;

import com.data.server.UserManager;
import com.dhis2.App;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SplashInteractor implements SplashContractsModule.Interactor {

    private SplashContractsModule.View view;
    private SplashContractsModule.Router router;
    private UserManager userManager;
    @NonNull
    private final CompositeDisposable compositeDisposable;

    SplashInteractor(SplashContractsModule.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
        this.router = new SplashRouter(view);

        if(((App)view.getContext().getApplicationContext()).getServerComponent()!=null) {

            this.userManager = ((App) view.getContext().getApplicationContext()).getServerComponent().userManager();
            isUserLoggedIn();
        }else
            router.navigateToLoginView();
    }

    @Override
    public void isUserLoggedIn() {
        if (userManager == null) {
            router.navigateToLoginView();
            return;
        }

        compositeDisposable.add(userManager.isUserLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((isUserLoggedIn) -> {
                    if (isUserLoggedIn) {
                        router.navigateToHomeView();
                    } else {
                        router.navigateToLoginView();
                    }
                }, Timber::e));
    }

    @Override
    public void destroy() {
        compositeDisposable.clear();

    }

}