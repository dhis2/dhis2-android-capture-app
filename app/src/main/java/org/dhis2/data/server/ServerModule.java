package org.dhis2.data.server;

import android.content.Context;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.dhis2.BuildConfig;
import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.prefs.PreferenceProviderImpl;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.analytics.AnalyticsInterceptor;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Configuration;
import org.hisp.dhis.android.core.D2Manager;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;

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
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new StethoInterceptor());
        interceptors.add(new AnalyticsInterceptor(
                new AnalyticsHelper(FirebaseAnalytics.getInstance(context),
                        new PreferenceProviderImpl(context))));
        return D2Configuration.builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .connectTimeoutInSeconds(3 * 60)
                .readTimeoutInSeconds(3 * 60)
                .networkInterceptors(interceptors)
                .writeTimeoutInSeconds(3 * 60)
                .context(context)
                .build();
    }
}
