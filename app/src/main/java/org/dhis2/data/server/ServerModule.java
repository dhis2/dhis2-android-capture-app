package org.dhis2.data.server;

import android.content.Context;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.dhis2.BuildConfig;
import org.dhis2.data.dagger.PerServer;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.d2manager.D2Configuration;
import org.hisp.dhis.android.core.d2manager.D2Manager;

import java.util.Collections;

import dagger.Module;
import dagger.Provides;

@Module
@PerServer
public class ServerModule {

    @Provides
    @PerServer
    D2 sdk() {
        return D2Manager.getD2();
    }

    @Provides
    @PerServer
    UserManager configurationRepository(D2 d2) {
        return new UserManagerImpl(d2);
    }

    @Provides
    @PerServer
    DataBaseExporter dataBaseExporter(D2 d2) {
        return new DataBaseExporterImpl(d2);
    }

    public static D2Configuration getD2Configuration(Context context) {
        return D2Configuration.builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .connectTimeoutInSeconds(3 * 60)
                .readTimeoutInSeconds(3 * 60)
                .networkInterceptors(Collections.singletonList(new StethoInterceptor()))
                .writeTimeoutInSeconds(3 * 60)
                .context(context)
                .build();
    }

}
