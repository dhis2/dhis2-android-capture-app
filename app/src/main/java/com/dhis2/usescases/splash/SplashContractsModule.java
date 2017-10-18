package com.dhis2.usescases.splash;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.dhis2.data.server.ServerComponent;
import com.dhis2.data.server.UserManager;
import com.dhis2.usescases.general.AbstractActivityContracts;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashContractsModule {

    /*private final UserManager userManager;

    SplashContractsModule(@Nullable ServerComponent serverComponent){
        this.userManager = serverComponent == null? null : serverComponent.userManager();
    }*/

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