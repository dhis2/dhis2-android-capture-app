package com.dhis2.usescases.splash;

import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashContractsModule {

    @Provides
    View provideView(SplashActivity splashActivity){
        return splashActivity;
    }

    @Provides
    Presenter providePresenter(View view) {
        return new SplashPresenter(view);
    }

    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {

    }

    interface Interactor {
        @UiThread
        void isUserLoggedIn();
        void destroy();
    }

    interface Router {
        @UiThread
        void navigateToLoginView();

        @UiThread
        void navigateToHomeView();
    }

}