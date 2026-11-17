package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Desktop and iOS build their `commonsModule` from these two modules alone, so this checks that
 * every binding common code asks for resolves there.
 */
class SdklessCommonsModuleTest {
    private val app = koinApplication { modules(sharedCommonsModule, sdklessCommonsModule) }

    @AfterTest
    fun tearDown() {
        app.close()
    }

    @Test
    fun `platforms without the SDK resolve every binding common code needs`() {
        val koin = app.koin

        assertNotNull(koin.getOrNull<Dispatcher>())
        assertNotNull(koin.getOrNull<SessionRenewalNotifier>())
        assertNotNull(koin.getOrNull<NetworkStatusProvider>())
        assertNotNull(koin.getOrNull<FeatureConfigRepository>())
        assertNotNull(koin.getOrNull<ValueParser>())
    }
}
