package org.dhis2.data.server

import android.content.Context
import android.os.Build

import com.facebook.stetho.okhttp3.StethoInterceptor

import org.dhis2.R
import org.dhis2.data.dagger.PerServer
import org.hisp.dhis.android.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.configuration.Configuration
import org.hisp.dhis.android.core.data.api.Authenticator
import org.hisp.dhis.android.core.data.api.BasicAuthenticatorFactory
import org.hisp.dhis.android.core.data.database.DatabaseAdapter

import java.security.KeyStore
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.TimeUnit

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.TlsVersion
import timber.log.Timber

@Module
@PerServer
class ServerModule(private val configuration: Configuration) {

    @Provides
    @PerServer
    internal fun sdk(databaseAdapter: DatabaseAdapter, client: OkHttpClient, context: Context): D2 {
        return D2.Builder()
                .configuration(configuration)
                .databaseAdapter(databaseAdapter)
                .okHttpClient(client)
                .context(context)
                .build()
    }

    @Provides
    @PerServer
    internal fun authenticator(databaseAdapter: DatabaseAdapter): Authenticator {
        return BasicAuthenticatorFactory.create(databaseAdapter)
    }

    @Provides
    @PerServer
    internal fun okHttpClient(authenticator: Authenticator, context: Context): OkHttpClient {
        val userAgent = String.format("%s/%s/%s/Android_%s",
                "Dhis2", //App name
                BuildConfig.VERSION_NAME, //SDK version
                org.dhis2.BuildConfig.VERSION_NAME, //App version
                Build.VERSION.SDK_INT //Android Version
        )
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 5
        val client = OkHttpClient.Builder()
                .addInterceptor(authenticator)
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val withUserAgent = originalRequest.newBuilder()
                            .header("User-Agent", userAgent)
                            .build()
                    Timber.d(originalRequest.url().encodedPath())
                    chain.proceed(withUserAgent)
                }
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .addNetworkInterceptor(StethoInterceptor())
                .dispatcher(dispatcher)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {

                val sc = SSLContext.getInstance("TLSv1.2")
                sc.init(null, null, null)

                val trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                    throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
                }
                val trustManager = trustManagers[0] as X509TrustManager

                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
                        /*.cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)*/
                        .build()

                val specs = ArrayList<ConnectionSpec>()
                specs.add(cs)
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)

                client
                        .sslSocketFactory(TLSSocketFactory(sc.socketFactory), trustManager)
                        .connectionSpecs(specs)

            } catch (e: Exception) {
                Timber.e(e)
            }

        }
        return client.build()
    }

    @Provides
    @PerServer
    internal fun configurationRepository(d2: D2): UserManager {
        return UserManagerImpl(d2)
    }

}
