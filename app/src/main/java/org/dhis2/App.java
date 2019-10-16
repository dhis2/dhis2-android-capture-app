package org.dhis2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.android.gms.security.ProviderInstaller;
import com.mapbox.mapboxsdk.Mapbox;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.dagger.PerUser;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.forms.FormComponent;
import org.dhis2.data.forms.FormModule;
import org.dhis2.data.prefs.PreferenceModule;
import org.dhis2.data.schedulers.SchedulerModule;
import org.dhis2.data.schedulers.SchedulersProviderImpl;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardComponent;
import org.dhis2.usescases.teiDashboard.TeiDashboardModule;
import org.dhis2.utils.UtilsModule;
import org.dhis2.utils.analytics.AnalyticsModule;
import org.dhis2.utils.timber.DebugTree;
import org.dhis2.utils.timber.ReleaseTree;
import org.hisp.dhis.android.core.d2manager.D2Manager;

import javax.inject.Singleton;

import io.fabric.sdk.android.Fabric;
import io.ona.kujaku.KujakuLibrary;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 27/09/2017.
 */

public class App extends MultiDexApplication implements Components {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected static final String DATABASE_NAME = "dhis.db";

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
    FormComponent formComponent;

    @Nullable
    @PerActivity
    LoginComponent loginComponent;

    @Nullable
    @PerActivity
    private TeiDashboardComponent dashboardComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(BuildConfig.DEBUG ? new DebugTree() : new ReleaseTree());
        long startTime = System.currentTimeMillis();
        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this);

        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);

        KujakuLibrary.setEnableMapDownloadResume(false);
        KujakuLibrary.init(this);

        Fabric.with(this, new Crashlytics());

        setUpAppComponent();
        setUpServerComponent();
//        setUpUserComponent();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            upgradeSecurityProvider();


        Scheduler asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> asyncMainThreadScheduler);
    }

    private void upgradeSecurityProvider() {
        try {
            ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
                @Override
                public void onProviderInstalled() {
                    Timber.e("New security provider installed.");
                }

                @Override
                public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                    Timber.e("New security provider install failed.");
                }
            });
        } catch (Exception ex) {
            Timber.e(ex, "Unknown issue trying to install a new security provider");
        }

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
        boolean isLogged = D2Manager.blockingInstantiateD2(ServerModule.getD2Configuration(this)).userModule().isLogged().blockingGet();

        serverComponent = appComponent.plus(new ServerModule(), new DbModule(DATABASE_NAME));

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
//                .dbModule(new DbModule(DATABASE_NAME))
                .appModule(new AppModule(this))
                .schedulerModule(new SchedulerModule(new SchedulersProviderImpl()))
                .analyticsModule(new AnalyticsModule())
                .preferenceModule(new PreferenceModule())
                .utilModule(new UtilsModule());
    }

    @NonNull
    protected AppComponent createAppComponent() {
        return (appComponent = prepareAppComponent().build());
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
    public LoginComponent createLoginComponent() {
        return (loginComponent = appComponent.plus(new LoginModule()));
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
            serverComponent = appComponent.plus(new ServerModule(), new DbModule(DATABASE_NAME));
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
    // Form component
    ////////////////////////////////////////////////////////////////////////

    @NonNull
    public FormComponent createFormComponent(@NonNull FormModule formModule) {
        return (formComponent = userComponent.plus(formModule));
    }

    @Nullable
    public FormComponent formComponent() {
        return formComponent;
    }

    public void releaseFormComponent() {
        formComponent = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Dashboard component
    ////////////////////////////////////////////////////////////////////////
    @NonNull
    public TeiDashboardComponent createDashboardComponent(@NonNull TeiDashboardModule dashboardModule) {
        return (dashboardComponent = userComponent.plus(dashboardModule));
    }

    @Nullable
    public TeiDashboardComponent dashboardComponent() {
        return dashboardComponent;
    }

    public void releaseDashboardComponent() {
        dashboardComponent = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // AndroidInjector
    ////////////////////////////////////////////////////////////////////////


    /**
     * Visible only for testing purposes.
     */
    public void setTestComponent(AppComponent testingComponent) {
        appComponent = testingComponent;
    }

}
