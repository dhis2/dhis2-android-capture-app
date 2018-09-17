package org.dhis2.data.server;

import android.content.Context;
import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerServer;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;
import org.hisp.dhis.android.core.data.api.Authenticator;
import org.hisp.dhis.android.core.data.api.BasicAuthenticatorFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
@PerServer
public class ServerModule {
    private final ConfigurationModel configuration;

    public ServerModule(@NonNull ConfigurationModel configuration) {
        this.configuration = configuration;
    }

    @Provides
    @PerServer
    D2 sdk(DatabaseAdapter databaseAdapter, OkHttpClient client, Context context) {
        return new D2.Builder()
                .configuration(configuration)
                .databaseAdapter(databaseAdapter)
                .okHttpClient(client)
                .context(context)
                .build();
    }

    @Provides
    @PerServer
    Authenticator authenticator(DatabaseAdapter databaseAdapter) {
        return BasicAuthenticatorFactory.create(databaseAdapter);
    }

    @Provides
    @PerServer
    OkHttpClient okHttpClient(Authenticator authenticator) {
        return new OkHttpClient.Builder()
                .addInterceptor(authenticator)
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    @Provides
    @PerServer
    UserManager configurationRepository(D2 d2) {
        return new UserManagerImpl(d2);
    }
}
