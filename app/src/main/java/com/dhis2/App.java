package com.dhis2;

import android.app.Application;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.data.dagger.PerActivity;
import com.data.dagger.PerServer;
import com.data.dagger.PerUser;
import com.data.database.DbModule;
import com.data.schedulers.SchedulerModule;
import com.data.schedulers.SchedulersProviderImpl;
import com.data.server.ServerComponent;
import com.data.server.ServerModule;
import com.data.server.UserManager;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import hu.supercluster.paperwork.Paperwork;

/**
 * Created by ppajuelo on 27/09/2017.
 */

public class App extends Application implements Components {
    private static final String DATABASE_NAME = "dhis.db";
    private static final String GIT_SHA = "gitSha";
    private static final String BUILD_DATE = "buildDate";

    @Inject
    Paperwork paperwork;

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
    LoginComponent loginComponent;

    @Nullable
    @PerActivity
    LoginTestComponent loginTestComponent;

    @Nullable
    @PerActivity
    FormComponent formComponent;

    @Nullable
    RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        setUpAppComponent();
        setUpServerComponent();
        setUpUserComponent();

       /* setUpLeakCanary();
        setUpFabric();
        setUpTimber();*/

        // do not allow to do work on main thread
        setUpStrictMode();

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

    private void setUpStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    /****************************************************************************************************
    *****------------------------------------------------------------------------------------------*****
                                              APP COMPONENT
    *****------------------------------------------------------------------------------------------*****
    ***************************************************************************************************/

    @NonNull
    protected DaggerComponent.Builder prepareAppComponent(){
        return DaggerAppComponent.builder()
                .dbModule(new DbModule(DATABASE_NAME))
                .appModule(new AppModule(this))
                .schedulerModule(new SchedulerModule(new SchedulersProviderImpl()));
    }

    @NonNull
    protected AppComponent createAppComponent() {
        return (appComponent = prepareAppComponent().build());
    }

    @NonNull
    @Override
    public AppComponent appComponent() {
        return null;
    }


    /****************************************************************************************************
    *****------------------------------------------------------------------------------------------*****
                                              LOGIN COMPONENT
    *****------------------------------------------------------------------------------------------*****
    ***************************************************************************************************/
    @NonNull
    @Override
    public LoginComponent createLoginComponent() {
        return null;
    }

    @Nullable
    @Override
    public LoginComponent loginComponent() {
        return null;
    }

    @Override
    public void releaseLoginComponent() {

    }

    @Override
    public void releaseLoginTestComponent() {

    }

    @NonNull
    @Override
    public ServerComponent createServerComponent(@NonNull ConfigurationModel configuration) {
        return null;
    }

    @Nullable
    @Override
    public ServerComponent serverComponent() {
        return null;
    }

    @Override
    public void releaseServerComponent() {

    }

    @NonNull
    @Override
    public UserComponent createUserComponent() {
        return null;
    }

    @Nullable
    @Override
    public UserComponent userComponent() {
        return null;
    }

    @Override
    public void releaseUserComponent() {

    }

    @NonNull
    @Override
    public FormComponent createFormComponent(@NonNull FormModule formModule) {
        return null;
    }

    @Nullable
    @Override
    public FormComponent formComponent() {
        return null;
    }

    @Override
    public void releaseFormComponent() {

    }
}
