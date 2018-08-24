package com.dhis2;

import android.databinding.ObservableBoolean;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.dagger.PerServer;
import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.database.DbModule;
import com.dhis2.data.forms.FormComponent;
import com.dhis2.data.forms.FormModule;
import com.dhis2.data.metadata.MetadataModule;
import com.dhis2.data.qr.QRModule;
import com.dhis2.data.schedulers.SchedulerModule;
import com.dhis2.data.schedulers.SchedulersProviderImpl;
import com.dhis2.data.server.ServerComponent;
import com.dhis2.data.server.ServerModule;
import com.dhis2.data.server.UserManager;
import com.dhis2.data.user.UserComponent;
import com.dhis2.data.user.UserModule;
import com.dhis2.usescases.login.LoginComponent;
import com.dhis2.usescases.login.LoginModule;
import com.dhis2.utils.UtilsModule;
import com.dhis2.utils.timber.DebugTree;
import com.facebook.stetho.Stetho;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric.sdk.android.Fabric;
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

    private ObservableBoolean metaSync = new ObservableBoolean(false);
    private ObservableBoolean dataSync = new ObservableBoolean(false);

    private static final String DATABASE_NAME = "dhis.db";

    private static App instance;

    @Inject
    ConfigurationManager configurationManager;

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

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Timber.plant(new DebugTree());
        Stetho.initializeWithDefaults(this);
        Fabric.with(this, new Crashlytics());

        this.instance = this;

        setUpAppComponent();
        setUpServerComponent();
        setUpUserComponent();

        Scheduler asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(),true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> asyncMainThreadScheduler);

    }

    private void setUpAppComponent() {

        appComponent = prepareAppComponent().build();
        appComponent.inject(this);

    }

    private void setUpServerComponent() {
        ConfigurationModel configuration = configurationManager.get();
        if (configuration != null) {
            serverComponent = appComponent.plus(new ServerModule(configuration));
        }
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
                .dbModule(new DbModule(DATABASE_NAME))
                .appModule(new AppModule(this))
                .schedulerModule(new SchedulerModule(new SchedulersProviderImpl()))
                .metadataModule(new MetadataModule())
                .qrModule(new QRModule())
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
    public ServerComponent createServerComponent(@NonNull ConfigurationModel configuration) {
        serverComponent = appComponent.plus(new ServerModule(configuration));
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
    // AndroidInjector
    ////////////////////////////////////////////////////////////////////////


    public static App getInstance() {
        return instance;
    }

    public boolean isSyncing() {
        return metaSync.get() || dataSync.get();
    }

    public void setMetaSync(boolean isSyncing) {
        this.metaSync.set(isSyncing);
    }

    public void seDataSync(boolean isSyncing) {
        this.dataSync.set(isSyncing);
    }

}
