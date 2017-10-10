package com.dhis2;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.data.dagger.PerServer;
import com.data.dagger.PerUser;
import com.data.database.DbModule;
import com.data.schedulers.SchedulerModule;
import com.data.schedulers.SchedulersProviderImpl;
import com.data.server.ServerComponent;
import com.data.server.ServerModule;
import com.data.server.UserManager;
import com.data.user.UserComponent;
import com.data.user.UserModule;

import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by ppajuelo on 27/09/2017.
 */

public class App extends Application implements HasActivityInjector {
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

    public ServerComponent getServerComponent() {
        return serverComponent;
    }

    public UserComponent getUserComponent(){
        return userComponent;
    }

    public ServerComponent createServerComponent(@NonNull ConfigurationModel configuration){
        return (serverComponent = appComponent.plus(new ServerModule(configuration)));
    }

    public UserComponent createUserComponent(){
        return (userComponent = serverComponent.plus(new UserModule()));
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {

        return activityDispatchingAndroidInjector;

    }

    public static App getInstance(){
        return instance;
    }

}
