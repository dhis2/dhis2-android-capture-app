package org.dhis2.data.server;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.dhis2.BuildConfig;
import org.dhis2.data.dagger.PerServer;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.api.authentication.internal.Authenticator;
import org.hisp.dhis.android.core.arch.api.authentication.internal.BasicAuthenticatorFactory;
import org.hisp.dhis.android.core.configuration.Configuration;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.TlsVersion;
import timber.log.Timber;

@Module
@PerServer
public class ServerModule {
    private final Configuration configuration;

    public ServerModule(@NonNull Configuration configuration) {
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
    OkHttpClient okHttpClient(Authenticator authenticator, Context context) {
        String userAgent = String.format("%s/%s/%s/Android_%s",
                "Dhis2", //App name
                BuildConfig.VERSION_NAME,//SDK version
                org.dhis2.BuildConfig.VERSION_NAME, //App version
                Build.VERSION.SDK_INT //Android Version
        );
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(5);
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .addInterceptor(authenticator)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request withUserAgent = originalRequest.newBuilder()
                            .header("User-Agent", userAgent)
                            .build();
                    Timber.d(originalRequest.url().encodedPath());
                    return chain.proceed(withUserAgent);
                })
                .readTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .addNetworkInterceptor(new StethoInterceptor())
                .dispatcher(dispatcher);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client
                        .sslSocketFactory(new TLSSocketFactory(sc.getSocketFactory()), trustManager)
                        .connectionSpecs(specs);

            } catch (Exception e) {
                Timber.e(e);
            }

        }
        return client.build();
    }

    @Provides
    @PerServer
    UserManager configurationRepository(D2 d2) {
        return new UserManagerImpl(d2);
    }

}
