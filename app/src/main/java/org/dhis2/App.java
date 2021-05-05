package org.dhis2;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import org.dhis2.data.appinspector.AppInspector;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.dagger.PerUser;
import org.dhis2.data.prefs.Preference;
import org.dhis2.data.prefs.PreferenceModule;
import org.dhis2.data.schedulers.SchedulerModule;
import org.dhis2.data.schedulers.SchedulersProviderImpl;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.service.workManager.WorkManagerModule;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.uicomponents.map.MapController;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginContracts;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import org.dhis2.usescases.teiDashboard.TeiDashboardModule;
import org.dhis2.utils.analytics.AnalyticsModule;
import org.dhis2.utils.reporting.CrashReportModule;
import org.dhis2.utils.session.PinModule;
import org.dhis2.utils.session.SessionComponent;
import org.dhis2.utils.timber.DebugTree;
import org.dhis2.utils.timber.ReleaseTree;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Manager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketException;

import javax.inject.Singleton;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

public class App extends MultiDexApplication implements Components, LifecycleObserver {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

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

        MapController.Companion.init(this, BuildConfig.MAPBOX_ACCESS_TOKEN);

        setUpAppComponent();
        Timber.plant(BuildConfig.DEBUG ? new DebugTree() : new ReleaseTree(appComponent.injectCrashReportController()));

        setUpServerComponent();
        setUpRxPlugin();
        initCustomCrashActivity();
    }

    private void initCustomCrashActivity() {
        CaocConfig.Builder.create()
                .errorDrawable(R.drawable.ic_dhis)
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
        D2 d2Configuration = D2Manager.blockingInstantiateD2(ServerModule.getD2Configuration(this));
        boolean isLogged = d2Configuration.userModule().isLogged().blockingGet();
        serverComponent = appComponent.plus(new ServerModule());

        if (isLogged)
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
                .workManagerController(new WorkManagerModule())
                .crashReportModule(new CrashReportModule());
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
    public LoginComponent createLoginComponent(LoginContracts.View view) {
        return (loginComponent = appComponent.plus(new LoginModule(view)));
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
        if (serverComponent == null)
            serverComponent = appComponent.plus(new ServerModule());
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
        return (sessionComponent = appComponent.plus(pinModule));
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
}
