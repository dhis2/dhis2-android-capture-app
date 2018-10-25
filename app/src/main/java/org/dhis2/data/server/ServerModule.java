package org.dhis2.data.server;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerServer;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;
import org.hisp.dhis.android.core.data.api.Authenticator;
import org.hisp.dhis.android.core.data.api.BasicAuthenticatorFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import timber.log.Timber;

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
    OkHttpClient okHttpClient(Authenticator authenticator, BriteDatabase briteDatabase) {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .addInterceptor(authenticator)
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (response.code() != 200) {
                        try {
                            JSONObject jsonObject = new JSONObject(chain.proceed(request).body().string());
                            Log.d("RESPONSE INTERCEPTOR", jsonObject.getString("message"));
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("errorDate", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                            contentValues.put("errorCode", jsonObject.getInt("httpStatusCode"));
                            contentValues.put("errorMessage", jsonObject.getString("message"));
                            if (jsonObject.has("response") && jsonObject.getJSONObject("response").has("importSummaries")) {
                                JSONArray importSummaries = jsonObject.getJSONObject("response").getJSONArray("importSummaries");
                                StringBuilder description = new StringBuilder();
                                for (int i = 0; i < importSummaries.length(); i++) {
                                    description.append(importSummaries.getJSONObject(i).getString("description"));
                                    if (i < importSummaries.length() - 1)
                                        description.append("\n");
                                }
                                contentValues.put("errorDescription", description.toString());
                            }
                            briteDatabase.insert("ErrorMessage", contentValues);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return response;
                });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {

                SSLContext sc = SSLContext.getInstance("TLS"/*"TLSv1.2"*/);
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
