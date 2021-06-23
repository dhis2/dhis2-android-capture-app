package org.dhis2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.featureconfig.di.FeatureConfigComponentProvider;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.user.UserComponent;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginContracts;

import dhis2.org.analytics.charts.di.AnalyticsComponentProvider;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentComponent;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule;

public interface Components extends FeatureConfigComponentProvider, AnalyticsComponentProvider {

    @NonNull
    AppComponent appComponent();

    ///////////////////////////////////////////////////////////////////
    // Login component
    ///////////////////////////////////////////////////////////////////


    @NonNull
    LoginComponent createLoginComponent(LoginContracts.View view);

    @Nullable
    LoginComponent loginComponent();

    void releaseLoginComponent();


    ////////////////////////////////////////////////////////////////////
    // Server component
    ///////////////////////////////////////////////////////////////////

    @NonNull
    ServerComponent createServerComponent();

    @Nullable
    ServerComponent serverComponent();

    void releaseServerComponent();

    ////////////////////////////////////////////////////////////////////
    // User component
    ////////////////////////////////////////////////////////////////////

    @NonNull
    UserComponent createUserComponent();

    @Nullable
    UserComponent userComponent();

    void releaseUserComponent();
}
