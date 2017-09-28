package com.dhis2.usescases.splash;

import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;

public class SplashContracts {

    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {
        @UiThread
        void isUserLoggedIn();
    }

    interface Interactor {

    }

    interface Router {
        @UiThread
        void navigateToLoginView();

        @UiThread
        void navigateToHomeView();
    }

}