package org.dhis2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.components.ComponentProvider;
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerComponentProvider;
import org.dhis2.commons.featureconfig.di.FeatureConfigComponentProvider;
import org.dhis2.commons.filters.di.FilterPresenterProvider;
import org.dhis2.commons.orgunitselector.OUTreeComponentProvider;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.user.UserComponent;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;

import dhis2.org.analytics.charts.di.AnalyticsComponentProvider;

public interface Components extends FeatureConfigComponentProvider,
        AnalyticsComponentProvider,
        CalendarPickerComponentProvider,
        FilterPresenterProvider,
        OUTreeComponentProvider,
        ComponentProvider {

    @NonNull
    AppComponent appComponent();

    ///////////////////////////////////////////////////////////////////
    // Login component
    ///////////////////////////////////////////////////////////////////


    @NonNull
    LoginComponent createLoginComponent(LoginModule loginModule);

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
