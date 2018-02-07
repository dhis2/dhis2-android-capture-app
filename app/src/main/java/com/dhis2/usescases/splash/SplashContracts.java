package com.dhis2.usescases.splash;

import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;

public class SplashContracts {

    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {
        void destroy();

        void init(View view);

        @UiThread
        void isUserLoggedIn();

        @UiThread
        void navigateToLoginView();

        @UiThread
        void navigateToHomeView();
    }

    interface Interactor {

    }

    interface Router {

    }

}