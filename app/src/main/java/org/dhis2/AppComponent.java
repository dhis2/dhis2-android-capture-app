package org.dhis2;

import org.dhis2.commons.featureconfig.di.FeatureConfigModule;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.network.NetworkUtilsModule;
import org.dhis2.data.dispatcher.DispatcherModule;
import org.dhis2.data.forms.dataentry.validation.ValidatorModule;
import org.dhis2.commons.locationprovider.LocationModule;
import org.dhis2.commons.locationprovider.LocationProvider;
import org.dhis2.commons.prefs.PreferenceModule;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerModule;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkManagerModule;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.usescases.splash.SplashComponent;
import org.dhis2.usescases.splash.SplashModule;
import org.dhis2.utils.Validator;
import org.dhis2.utils.analytics.AnalyticsModule;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsModule;
import org.dhis2.commons.filters.di.FilterModule;
import org.dhis2.commons.reporting.CrashReportController;
import org.dhis2.commons.reporting.CrashReportModule;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Map;

import javax.inject.Singleton;

import dagger.Component;
import dispatch.core.DispatcherProvider;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Singleton
@Component(modules = {
        AppModule.class,
        SchedulerModule.class,
        AnalyticsModule.class,
        PreferenceModule.class,
        WorkManagerModule.class,
        MatomoAnalyticsModule.class,
        ValidatorModule.class,
        CrashReportModule.class,
        LocationModule.class,
        DispatcherModule.class,
        FeatureConfigModule.class,
        NetworkUtilsModule.class,
        CustomDispatcherModule.class
})
public  interface AppComponent {

    @Component.Builder
    interface Builder {
        Builder appModule(AppModule appModule);

        Builder schedulerModule(SchedulerModule schedulerModule);

        Builder analyticsModule(AnalyticsModule module);

        Builder preferenceModule(PreferenceModule preferenceModule);

        Builder workManagerController(WorkManagerModule workManagerModule);

        Builder crashReportModule(CrashReportModule crashReportModule);

        Builder coroutineDispatchers(DispatcherModule dispatcherModule);

        Builder featureConfigModule(FeatureConfigModule featureConfigModule);

        Builder networkUtilsModule(NetworkUtilsModule networkUtilsModule);

        Builder customDispatcher(CustomDispatcherModule dispatcherProvider);

        AppComponent build();
    }

    Map<ValueType, Validator> injectValidators();

    CrashReportController injectCrashReportController();

    PreferenceProvider preferenceProvider();

    WorkManagerController workManagerController();

    MatomoAnalyticsController matomoController();

    org.dhis2.commons.viewmodel.DispatcherProvider dispatcherProvider();

    LocationProvider locationProvider();

    NetworkUtils networkUtilsProvider();

    DispatcherProvider customDispatcherProvider();

    //injection targets
    void inject(App app);

    //sub-components
    ServerComponent plus(ServerModule serverModule);

    SplashComponent plus(SplashModule module);

    LoginComponent plus(LoginModule loginContractsModule);
}
