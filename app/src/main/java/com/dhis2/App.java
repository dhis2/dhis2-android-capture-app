package com.dhis2;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;

import com.dhis2.data.dagger.PerServer;
import com.dhis2.data.dagger.PerUser;
import com.dhis2.data.database.DbModule;
import com.dhis2.data.schedulers.SchedulerModule;
import com.dhis2.data.schedulers.SchedulersProviderImpl;
import com.dhis2.data.server.ServerComponent;
import com.dhis2.data.server.ServerModule;
import com.dhis2.data.server.UserManager;
import com.dhis2.data.user.UserComponent;
import com.dhis2.data.user.UserModule;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by ppajuelo on 27/09/2017.
 */

public class App extends MultiDexApplication implements HasActivityInjector {
    private static final String DATABASE_NAME = "dhis.db";

    private static App instance;
    @Inject
    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;


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


    @Override
    public void onCreate() {
        super.onCreate();

        this.instance = this;

        setUpAppComponent();
        setUpServerComponent();
        setUpUserComponent();

    }

    private void setUpAppComponent() {
        appComponent = DaggerAppComponent
                .builder()
                .application(this)
                .database(new DbModule(DATABASE_NAME))
                .scheduler(new SchedulerModule(new SchedulersProviderImpl()))
                .build();
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
    public AppComponent appComponent() {
        return appComponent;
    }

    ////////////////////////////////////////////////////////////////////////
    // Server component
    ////////////////////////////////////////////////////////////////////////

    public ServerComponent createServerComponent(@NonNull ConfigurationModel configuration) {
        return (serverComponent = appComponent.plus(new ServerModule(configuration)));
    }

    public ServerComponent getServerComponent() {
        return serverComponent;
    }

    ////////////////////////////////////////////////////////////////////////
    // User component
    ////////////////////////////////////////////////////////////////////////


    public UserComponent createUserComponent() {
        return (userComponent = serverComponent.plus(new UserModule()));
    }

    public UserComponent getUserComponent() {
//        if (userComponent == null)
//            createUserComponent();
        return userComponent;
    }


    ////////////////////////////////////////////////////////////////////////
    // AndroidInjector
    ////////////////////////////////////////////////////////////////////////


    public static App getInstance() {
        return instance;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityDispatchingAndroidInjector;
    }
}
