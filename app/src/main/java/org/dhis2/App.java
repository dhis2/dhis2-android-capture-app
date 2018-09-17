package org.dhis2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.dagger.PerUser;
import org.dhis2.data.database.DbModule;
import org.dhis2.data.forms.FormComponent;
import org.dhis2.data.forms.FormModule;
import org.dhis2.data.metadata.MetadataModule;
import org.dhis2.data.qr.QRModule;
import org.dhis2.data.schedulers.SchedulerModule;
import org.dhis2.data.schedulers.SchedulersProviderImpl;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.user.UserComponent;
import org.dhis2.data.user.UserModule;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.utils.UtilsModule;
import org.dhis2.utils.timber.DebugTree;
import com.facebook.stetho.Stetho;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.fabric.sdk.android.Fabric;
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

}
