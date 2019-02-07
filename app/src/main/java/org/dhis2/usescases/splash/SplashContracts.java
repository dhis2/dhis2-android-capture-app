package org.dhis2.usescases.splash;

import androidx.annotation.UiThread;

import org.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

public class SplashContracts {

    interface View extends AbstractActivityContracts.View {

        Consumer<Integer> renderFlag();
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