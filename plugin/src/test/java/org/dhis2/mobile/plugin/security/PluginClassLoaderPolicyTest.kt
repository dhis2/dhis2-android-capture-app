package org.dhis2.mobile.plugin.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The deny list is the only thing standing between a plugin and the unrestricted SDK, so it is
 * pinned by name rather than by rule — a refactor that widens a prefix should break a test, not
 * quietly open a door.
 */
class PluginClassLoaderPolicyTest {
    private fun assertDenied(vararg classNames: String) =
        classNames.forEach { name ->
            assertFalse("should be denied: $name", PluginClassLoaderPolicy.isAllowed(name))
        }

    private fun assertAllowed(vararg classNames: String) =
        classNames.forEach { name ->
            assertTrue("should be allowed: $name", PluginClassLoaderPolicy.isAllowed(name))
        }

    @Test
    fun `denies the unrestricted SDK entry points`() {
        // D2Manager.getD2() is a public static: without this, every scope is decorative.
        assertDenied(
            "org.hisp.dhis.android.core.D2",
            "org.hisp.dhis.android.core.D2Manager",
            "org.hisp.dhis.android.core.D2Configuration",
            "org.hisp.dhis.android.core.arch.d2.internal.D2DIComponent",
        )
    }

    @Test
    fun `denies raw database and network access`() {
        assertDenied(
            "org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter",
            "org.hisp.dhis.android.core.arch.api.HttpServiceClient",
        )
    }

    @Test
    fun `denies the modules that would let a plugin widen or destroy its own grant`() {
        assertDenied(
            // Host configuration lives in the dataStore — including the plugin config itself.
            "org.hisp.dhis.android.core.datastore.DataStoreModule",
            "org.hisp.dhis.android.core.wipe.WipeModule",
            "org.hisp.dhis.android.core.user.UserModule",
        )
    }

    @Test
    fun `denies Koin's global service locator`() {
        assertDenied(
            "org.koin.core.context.GlobalContext",
            "org.koin.mp.KoinPlatformTools",
        )
    }

    @Test
    fun `denies the host application's own classes`() {
        assertDenied(
            "org.dhis2.usescases.main.MainViewModel",
            "org.dhis2.mobile.plugin.security.ScopedDhis2PluginContextFactory",
            "org.dhis2.commons.prefs.PreferenceProvider",
        )
    }

    @Test
    fun `allows the plugin API, which must resolve to the host's own copy`() {
        // Loading a second copy is what produces "not assignable to Dhis2Plugin".
        assertAllowed(
            "org.dhis2.mobile.plugin.sdk.Dhis2Plugin",
            "org.dhis2.mobile.plugin.sdk.Dhis2PluginContext",
            "org.dhis2.mobile.plugin.sdk.PluginMetadata",
        )
    }

    @Test
    fun `allows the SDK model and repository types a plugin legitimately uses`() {
        assertAllowed(
            "org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance",
            "org.hisp.dhis.android.core.event.EventCollectionRepository",
            "org.hisp.dhis.android.core.scopedaccess.ScopedD2",
            "org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector",
            "org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode",
        )
    }

    @Test
    fun `denies SDK-private packages`() {
        assertDenied(
            "org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceStore",
            "org.hisp.dhis.android.core.datavalue.internal.DataValueStore",
        )
    }

    @Test
    fun `leaves unrelated classes alone`() {
        // Kotlin, Compose and the platform all resolve from the host as usual.
        assertAllowed(
            "kotlin.collections.CollectionsKt",
            "androidx.compose.runtime.Composer",
            "java.lang.String",
            "com.example.someplugin.MyPlugin",
        )
    }
}
