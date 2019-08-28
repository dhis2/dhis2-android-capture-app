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

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.dagger.PerUser;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.forms.FormComponent;
import org.dhis2.data.forms.FormModule;
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
import org.dhis2.utils.timber.DebugTree;
import org.dhis2.utils.timber.ReleaseTree;
import org.hisp.dhis.android.core.d2manager.D2Manager;

import javax.inject.Singleton;

import io.fabric.sdk.android.Fabric;
import io.ona.kujaku.KujakuLibrary;
import io.reactivex.Scheduler;
import io.reactivex.Single;
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

    private static final String DATABASE_NAME = "dhis.db";

    private static App instance;

    @NonNull
    @Singleton
    AppComponent appComponent;

    @Nullable
    @PerServer
    ServerComponent serverComponent;

    @Nullable
    @PerUser
    UserComponent userComponent;

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
        Timber.d("APPLICATION INITIALIZATION");
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
            Timber.d("STETHO INITIALIZATION END AT %s", System.currentTimeMillis() - startTime);
        }

        KujakuLibrary.setEnableMapDownloadResume(false);
        KujakuLibrary.init(this);

        Fabric.with(this, new Crashlytics());
        Timber.d("FABRIC INITIALIZATION END AT %s", System.currentTimeMillis() - startTime);

        this.instance = this;

        setUpAppComponent();
        setUpServerComponent();
//        setUpUserComponent();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            upgradeSecurityProvider();
            Timber.d("SECURITY INITIALIZATION END AT %s", System.currentTimeMillis() - startTime);
        }

        Scheduler asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> asyncMainThreadScheduler);
        Timber.d("RXJAVAPLUGIN INITIALIZATION END AT %s", System.currentTimeMillis() - startTime);

        Timber.d("APPLICATION INITIALIZATION END AT %s", System.currentTimeMillis() - startTime);
        Timber.d("APPLICATION INITIALIZATION END AT %s", System.currentTimeMillis());
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

    private void setUpServerComponent() {
        boolean isLogged = D2Manager.setUp(ServerModule.getD2Configuration(this))
                .andThen(
                        Single.defer(() -> {
                            if (D2Manager.isServerUrlSet())
                                return D2Manager.instantiateD2().flatMap(d2 -> d2.userModule().isLogged());
                            else
                                return Single.just(false);

                        })
                ).blockingGet();
        if (D2Manager.isServerUrlSet())
            serverComponent = appComponent.plus(new ServerModule(),new DbModule(DATABASE_NAME));

        if (isLogged)
            setUpUserComponent();
    }


    private void setUpUserComponent() {
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
        serverComponent = appComponent.plus(new ServerModule(),new DbModule(DATABASE_NAME));
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


    public static App getInstance() {
        return instance;
    }

    /**
     * Visible only for testing purposes.
     */
    public void setTestComponent(AppComponent testingComponent) {
        appComponent = testingComponent;
    }

}
