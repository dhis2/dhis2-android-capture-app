package org.dhis2.data.server

import io.reactivex.Observable
import okhttp3.HttpUrl
import org.hisp.dhis.android.core.configuration.Configuration
import org.hisp.dhis.android.core.configuration.ConfigurationManager
import java.util.concurrent.Callable

class ConfigurationRepositoryImpl(
        val configurationManager: ConfigurationManager
): ConfigurationRepository {
    override fun configure(baseUrl: HttpUrl): Observable<Configuration> {
        return Observable.defer {
            Observable.fromCallable {
                val configuration = Configuration.builder().serverUrl(baseUrl).build()
                configurationManager.configure(configuration)
                configuration
            }
        }
    }

    override fun get(): Observable<Configuration> {
        return Observable.defer {
            val configuration = configurationManager.get()
            return@defer if (configuration != null) {
                Observable.just(configuration)
            } else Observable.empty()
        }
    }

    override fun remove(): Observable<Int> {
        return Observable.defer { Observable.fromCallable { configurationManager.remove() } }
    }

}