package org.dhis2.data.server

import org.hisp.dhis.android.core.configuration.Configuration
import io.reactivex.Observable
import okhttp3.HttpUrl

interface ConfigurationRepository {

    fun configure(baseUrl: HttpUrl): Observable<Configuration>

    fun get(): Observable<Configuration>

    fun remove(): Observable<Int>
}
