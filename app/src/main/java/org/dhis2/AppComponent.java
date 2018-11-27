package org.dhis2;

import org.dhis2.data.database.DbModule;
import org.dhis2.data.metadata.MetadataModule;
import org.dhis2.data.qr.QRModule;
import org.dhis2.data.schedulers.SchedulerModule;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.ServerModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureModule;
import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.login.LoginModule;
import org.dhis2.usescases.splash.SplashComponent;
import org.dhis2.usescases.splash.SplashModule;
import org.dhis2.utils.UtilsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ppajuelo on 10/10/2017.
 */
@Singleton
@Component(modules = {
        AppModule.class, DbModule.class, SchedulerModule.class, UtilsModule.class, MetadataModule.class, QRModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        Builder appModule(AppModule appModule);

        Builder dbModule(DbModule dbModule);

        Builder schedulerModule(SchedulerModule schedulerModule);

        Builder utilModule(UtilsModule utilsModule);

        Builder metadataModule(MetadataModule metadataModule);

        Builder qrModule(QRModule qrModule);

        AppComponent build();
    }

    //injection targets
    void inject(App app);

    //sub-components
    ServerComponent plus(ServerModule serverModule);

    SplashComponent plus(SplashModule module);

    LoginComponent plus(LoginModule loginContractsModule);

}
