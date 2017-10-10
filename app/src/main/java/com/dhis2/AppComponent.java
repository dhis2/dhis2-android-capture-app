package com.dhis2;

import android.app.Application;

import com.data.database.DbModule;
import com.data.schedulers.SchedulerModule;
import com.data.server.ServerComponent;
import com.data.server.ServerModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Singleton
@Component(modules = {AndroidInjectionModule.class,
        AppModule.class,
        DbModule.class,
        SchedulerModule.class,
        ActivityBuilder.class})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Builder database(DbModule db);

        Builder scheduler(SchedulerModule schedulerModule);

        AppComponent build();
    }

    void inject(App app);

    ServerComponent plus(ServerModule serverModule);

}
