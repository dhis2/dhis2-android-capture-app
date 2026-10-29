package org.dhis2.usescases.about.data

import kotlinx.coroutines.withContext
import org.dhis2.BuildConfig
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.usescases.about.domain.AboutRepository
import org.dhis2.usescases.about.model.AboutInfo
import org.hisp.dhis.android.core.D2

private const val RELEASE_BUILD_TYPE = "release"
private const val HTTPS_SCHEME = "https://"

class AboutRepositoryImpl(
    private val d2: D2,
    private val dispatcher: Dispatcher,
    private val domainErrorMapper: DomainErrorMapper,
) : AboutRepository {
    override suspend fun aboutInfo(): AboutInfo =
        execute {
            AboutInfo(
                version = BuildConfig.VERSION_NAME,
                buildHash = BuildConfig.GIT_SHA.takeIf { BuildConfig.BUILD_TYPE != RELEASE_BUILD_TYPE },
                sdkVersion = BuildConfig.SDK_VERSION,
                serverUrl = serverUrl(),
                username = username(),
            )
        }

    /**
     * The scheme is dropped so the row reads as the design specifies. A non https server keeps its
     * scheme, as that is worth showing to whoever is reading the screen for support.
     */
    private fun serverUrl(): String =
        d2
            .systemInfoModule()
            .systemInfo()
            .blockingGet()
            ?.contextPath()
            .orEmpty()
            .removePrefix(HTTPS_SCHEME)

    private fun username(): String =
        d2
            .userModule()
            .user()
            .blockingGet()
            ?.username()
            .orEmpty()

    private suspend fun <T> execute(block: suspend () -> T): T =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors(block)
        }
}
