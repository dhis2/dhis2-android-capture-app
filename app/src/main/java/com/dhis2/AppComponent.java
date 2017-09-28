package com.dhis2;


import com.data.database.DbModule;
import com.data.schedulers.SchedulerModule;
import com.data.server.ServerComponent;
import com.data.server.ServerModule;
import com.data.utils.UtilsModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class, DbModule.class, SchedulerModule.class, UtilsModule.class
})
public interface AppComponent {
    // exposing objects for testing
    BriteDatabase briteDatabase();

    // injection targets
    void inject(App dhisApp);

    // sub-components
    ServerComponent plus(ServerModule serverModule);

    LauncherComponent plus(LauncherModule launcherModule);

    LoginComponent plus(LoginModule loginModule);
}
