package org.dhis2;

import static org.dhis2.utils.analytics.AnalyticsConstants.DATA_STORE_ANALYTICS_PERMISSION_KEY;

import android.content.Context;
import android.os.Looper;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.di.dagger.PerServer;
import org.dhis2.commons.di.dagger.PerUser;
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerComponent;
import org.dhis2.commons.dialogs.calendarpicker.di.CalendarPickerModule;
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityComponent;
import org.dhis2.commons.featureconfig.di.FeatureConfigActivityModule;
import org.dhis2.commons.featureconfig.di.FeatureConfigModule;
import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.commons.network.NetworkUtilsModule;
import org.dhis2.commons.orgunitselector.OUTreeComponent;
import org.dhis2.commons.orgunitselector.OUTreeModule;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceModule;
import org.dhis2.commons.reporting.CrashReportModule;
import org.dhis2.commons.schedulers.SchedulerModule;
import org.dhis2.commons.schedulers.SchedulersProviderImpl;
import org.dhis2.commons.sync.SyncComponentProvider;
import org.dhis2.data.appinspector.AppInspector;
import org.dhis2.data.dispatcher.DispatcherModule;
import org.dhis2.data.server.SSLContextInitializer;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.service.workManager.WorkManagerModule;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.maps.MapController;
import org.dhis2.usescases.crash.CrashActivity;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import org.dhis2.usescases.teiDashboard.TeiDashboardModule;
import org.dhis2.utils.analytics.AnalyticsModule;
import org.dhis2.utils.granularsync.SyncStatusDialogProvider;
import org.dhis2.utils.session.PinModule;
import org.dhis2.utils.session.SessionComponent;
import org.dhis2.utils.timber.DebugTree;
import org.hisp.dhis.android.core.D2Manager;
import org.hisp.dhis.android.core.datastore.KeyValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketException;

import javax.inject.Singleton;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dagger.hilt.android.HiltAndroidApp;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentComponent;
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.sentry.SentryLevel;
import io.sentry.android.core.SentryAndroid;
import timber.log.Timber;

@HiltAndroidApp
public class App extends MultiDexApplication implements Components, LifecycleObserver {

    @NonNull
    @Singleton
    AppComponent appComponent;

    @Nullable
    @PerServer
    protected ServerComponent serverComponent;

    @Nullable
    @PerUser
    protected UserComponent userComponent;

    @Nullable
    @PerActivity
    LoginComponent loginComponent;

    @Nullable
    @PerActivity
    private TeiDashboardComponent dashboardComponent;

    @Nullable
    private SessionComponent sessionComponent;

    private boolean fromBackGround = false;
    private boolean recreated;
    private AppInspector appInspector;

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        appInspector = new AppInspector(this).init();

        MapController.Companion.init(this);

        setUpAppComponent();
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }

        setUpSecurityProvider();
        setUpServerComponent();
        initCrashController();
        setUpRxPlugin();
        initCustomCrashActivity();
    }

    public void initCrashController() {
        if (areTrackingPermissionGranted()) {
            SentryAndroid.init(this, options -> {
                options.setDsn(BuildConfig.SENTRY_DSN);
                options.setAnrReportInDebug(true);

                // Add a callback that will be used before the event is sent to Sentry.
                // With this callback, you can modify the event or, when returning null, also discard the event.
                options.setBeforeSend((event, hint) -> {
                    if (SentryLevel.DEBUG.equals(event.getLevel()))
                        return null;
                    else
                        return event;
                });
                options.setEnvironment(BuildConfig.DEBUG ? "debug" : "production");
                options.setDebug(BuildConfig.DEBUG);
                // Enable view hierarchy for crashes
                options.setAttachViewHierarchy(true);
                // Enable the performance API by setting a sample-rate
                options.setTracesSampleRate(BuildConfig.DEBUG ? 1.0 : 0.1);
                // Enable profiling when starting transactions
                options.setProfilesSampleRate(BuildConfig.DEBUG ? 1.0 : 0.1);
            });
        }
    }

    private void setUpSecurityProvider() {
        SSLContextInitializer.INSTANCE.initializeSSLContext(this);
    }

    private void initCustomCrashActivity() {
        CaocConfig.Builder.create()
                .errorActivity(CrashActivity.class)
                .apply();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void setUpAppComponent() {
        appComponent = prepareAppComponent().build();
        appComponent.inject(this);
    }

    protected void setUpServerComponent() {
        serverComponent = appComponent.plus(new ServerModule());
        if (Boolean.TRUE.equals(serverComponent.userManager().isUserLoggedIn().blockingFirst()))
            setUpUserComponent();
    }


    protected void setUpUserComponent() {
        UserManager userManager = serverComponent == null
                ? null : serverComponent.userManager();
        if (userManager != null && userManager.isUserLoggedIn().blockingFirst()) {
            userComponent = serverComponent.plus(new UserModule());
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // App component
    ////////////////////////////////////////////////////////////////////////
    @NonNull
    protected AppComponent.Builder prepareAppComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .schedulerModule(new SchedulerModule(new SchedulersProviderImpl()))
                .analyticsModule(new AnalyticsModule())
                .preferenceModule(new PreferenceModule())
                .networkUtilsModule(new NetworkUtilsModule())
                .workManagerController(new WorkManagerModule())
                .coroutineDispatchers(new DispatcherModule())
                .crashReportModule(new CrashReportModule())
                .customDispatcher(new CustomDispatcherModule())
                .featureConfigModule(new FeatureConfigModule());
    }

    @NonNull
    @Override
    public AppComponent appComponent() {
        return appComponent;
    }

    ////////////////////////////////////////////////////////////////////////
    // Login component
    ////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public LoginComponent createLoginComponent(LoginModule loginModule) {
        return (loginComponent = appComponent.plus(loginModule));
    }

    @Nullable
    @Override
    public LoginComponent loginComponent() {
        return loginComponent;
    }

    @Override
    public void releaseLoginComponent() {
        loginComponent = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Server component
    ////////////////////////////////////////////////////////////////////////

    @Override
    public ServerComponent createServerComponent() {
        if (!D2Manager.INSTANCE.isD2Instantiated())
            setUpServerComponent();
        return serverComponent;
    }

    @Nullable
    @Override
    public ServerComponent serverComponent() {
        return serverComponent;
    }

    @Override
    public void releaseServerComponent() {
        serverComponent = null;
    }

    @Nullable
    public ServerComponent getServerComponent() {
        return serverComponent;
    }

    ////////////////////////////////////////////////////////////////////////
    // User component
    ////////////////////////////////////////////////////////////////////////

    @Override
    public UserComponent createUserComponent() {
        return (userComponent = serverComponent.plus(new UserModule()));
    }

    @Override
    public UserComponent userComponent() {
        return userComponent;
    }

    @Override
    public void releaseUserComponent() {
        userComponent = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Dashboard component
    ////////////////////////////////////////////////////////////////////////
    @NonNull
    public TeiDashboardComponent createDashboardComponent(@NonNull TeiDashboardModule dashboardModule) {
        if (dashboardComponent != null) {
            this.recreated = true;
        }
        dashboardComponent = userComponent.plus(dashboardModule);
        return dashboardComponent;
    }

    @Nullable
    public TeiDashboardComponent dashboardComponent() {
        return dashboardComponent;
    }

    public void releaseDashboardComponent() {
        if (!this.recreated) {
            dashboardComponent = null;
        } else {
            recreated = false;
        }
    }

    @NotNull
    public SessionComponent createSessionComponent(PinModule pinModule) {
        return (sessionComponent = userComponent.plus(pinModule));
    }

    public void releaseSessionComponent() {
        sessionComponent = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Timber.tag("BG").d("App in background");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        Timber.tag("BG").d("App in foreground");
        fromBackGround = true;
    }

    public void disableBackGroundFlag() {
        fromBackGround = false;
    }

    public boolean isSessionBlocked() {
        boolean shouldShowPinDialog = fromBackGround && appComponent().preferenceProvider().getBoolean(Preference.SESSION_LOCKED, false);
        fromBackGround = false;
        return shouldShowPinDialog;
    }

    private void setUpRxPlugin() {
        Scheduler asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> asyncMainThreadScheduler);
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                Timber.d("Error in app");
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
            }
            if (e instanceof IllegalStateException) {
                Timber.d("Error in RxJava");
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
            }
            Timber.d(e);
        });
    }

    public AppInspector getAppInspector() {
        return appInspector;
    }

    @Override
    public FeatureConfigActivityComponent provideFeatureConfigActivityComponent() {
        return userComponent.plus(new FeatureConfigActivityModule());
    }

    @Override
    public CalendarPickerComponent provideCalendarPickerComponent() {
        return userComponent.plus(new CalendarPickerModule());
    }

    @Override
    public AnalyticsFragmentComponent provideAnalyticsFragmentComponent(AnalyticsFragmentModule module) {
        return userComponent.plus(module);
    }

    @Override
    public FilterPresenter provideFilterPresenter() {
        return userComponent.filterPresenter();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public OUTreeComponent provideOUTreeComponent(@NotNull OUTreeModule module) {
        return serverComponent.plus(module);
    }


    @NonNull
    @Override
    public SyncComponentProvider getSyncComponentProvider() {
        return new SyncStatusDialogProvider();
    }

    private boolean areTrackingPermissionGranted() {
        boolean isUserLoggedIn = serverComponent != null &&
                serverComponent.userManager().isUserLoggedIn().blockingFirst();
        if (!D2Manager.isD2Instantiated() || !isUserLoggedIn) {
            return false;
        }
        KeyValuePair granted = D2Manager.getD2().dataStoreModule().localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY).blockingGet();
        return granted != null && Boolean.parseBoolean(granted.value());
    }
}
