package com.dhis2;

import android.app.Application;

import com.dhis2.data.database.DbModule;
import com.dhis2.data.schedulers.SchedulerModule;
import com.dhis2.data.server.ServerComponent;
import com.dhis2.data.server.ServerModule;

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
        BindingModule.class})
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
