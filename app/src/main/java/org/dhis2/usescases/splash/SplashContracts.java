package org.dhis2.usescases.splash;

import org.dhis2.usescases.general.AbstractActivityContracts;

import androidx.annotation.UiThread;
import io.reactivex.functions.Consumer;

public class SplashContracts {

    interface SplashView extends AbstractActivityContracts.View {

        Consumer<Integer> renderFlag();
    }

    interface SplashPresenter {
        void destroy();

        void init(SplashView view);

        @UiThread
        void isUserLoggedIn();

        @UiThread
        void navigateToLoginView();

        @UiThread
        void navigateToHomeView();
    }
}