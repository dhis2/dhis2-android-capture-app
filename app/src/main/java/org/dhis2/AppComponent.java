package org.dhis2;

import org.dhis2.commons.featureconfig.di.FeatureConfigModule;
import org.dhis2.commons.locationprovider.LocationModule;
import org.dhis2.commons.locationprovider.LocationProvider;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.network.NetworkUtilsModule;
import org.dhis2.commons.prefs.PreferenceModule;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.reporting.CrashReportModule;
import org.dhis2.commons.schedulers.SchedulerModule;
import org.dhis2.commons.service.SessionManagerModule;
import org.dhis2.commons.service.SessionManagerService;
import org.dhis2.data.dispatcher.DispatcherModule;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.service.workManager.WorkManagerModule;
import org.dhis2.mobile.commons.reporting.CrashReportController;
import org.dhis2.usescases.splash.SplashComponent;
import org.dhis2.usescases.splash.SplashModule;
import org.dhis2.utils.analytics.AnalyticsModule;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsModule;
import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        SchedulerModule.class,
        AnalyticsModule.class,
        PreferenceModule.class,
        WorkManagerModule.class,
        CrashReportModule.class,
        SessionManagerModule.class,
        MatomoAnalyticsModule.class,
        LocationModule.class,
        DispatcherModule.class,
        FeatureConfigModule.class,
        NetworkUtilsModule.class,
})
public  interface AppComponent {

    @Component.Builder
    interface Builder {
        Builder appModule(AppModule appModule);

        Builder schedulerModule(SchedulerModule schedulerModule);

        Builder analyticsModule(AnalyticsModule module);

        Builder preferenceModule(PreferenceModule preferenceModule);

        Builder workManagerController(WorkManagerModule workManagerModule);

        Builder crashReportController(CrashReportModule crashReportModule);

        Builder sessionManagerService(SessionManagerModule sessionManagerModule);

        Builder coroutineDispatchers(DispatcherModule dispatcherModule);

        Builder featureConfigModule(FeatureConfigModule featureConfigModule);

        Builder networkUtilsModule(NetworkUtilsModule networkUtilsModule);

        AppComponent build();
    }

    PreferenceProvider preferenceProvider();

    WorkManagerController workManagerController();

    CrashReportController crashReportController();

    SessionManagerService sessionManagerService();

    MatomoAnalyticsController matomoController();

    org.dhis2.commons.viewmodel.DispatcherProvider dispatcherProvider();

    LocationProvider locationProvider();

    NetworkUtils networkUtilsProvider();

    //injection targets
    void inject(App app);

    //sub-components
    ServerComponent plus(ServerModule serverModule);

    SplashComponent plus(SplashModule module);
}
