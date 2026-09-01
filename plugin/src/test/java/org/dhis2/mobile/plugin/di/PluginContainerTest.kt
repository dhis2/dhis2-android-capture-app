package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.dto.DataValueDto
import org.dhis2.mobile.plugin.sdk.dto.TrackedEntityInstanceDto
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.scopedaccess.ScopedD2
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.koin.core.KoinApplication
import org.koin.core.error.NoDefinitionFoundException
import org.koin.dsl.module
import org.mockito.kotlin.mock

/**
 * Specifies what a plugin's private Koin container holds.
 *
 * The container used to be seeded with nothing at all, which is safe but unusable: a plugin's
 * repository had no way to obtain the one object it exists to wrap. It is now seeded with exactly
 * the plugin's own gateway — the same [ScopedD2] and [PluginMetadata] the plugin already receives
 * through [Dhis2PluginContext] — and nothing else. That is not a widening: it is the same two
 * objects, reachable by injection instead of by parameter threading.
 *
 * The negative cases matter as much as the positive ones, so both are pinned here.
 */
class PluginContainerTest {
    private val scopedD2: ScopedD2 = mock()

    private val metadata =
        PluginMetadata(
            id = "org.myorg.my-plugin",
            version = "1.0.0",
            entryPoint = "org.myorg.plugin.MyPlugin",
        )

    private val context = FakePluginContext(metadata, scopedD2)

    private var container: KoinApplication? = null

    @After
    fun tearDown() {
        container?.close()
    }

    private fun create(pluginModule: org.koin.core.module.Module? = null): KoinApplication =
        PluginContainer.create(context, pluginModule).also { container = it }

    @Test
    fun `hand the plugin its scoped SDK`() {
        assertSame(scopedD2, create().koin.get<ScopedD2>())
    }

    @Test
    fun `hand the plugin its server-authored metadata`() {
        assertEquals(metadata, create().koin.get<PluginMetadata>())
    }

    @Test
    fun `hand the plugin its context`() {
        assertSame(context, create().koin.get<Dhis2PluginContext>())
    }

    @Test
    fun `resolve a plugin binding that depends on the seeded SDK`() {
        // The whole point of seeding: a plugin repository can be constructed by Koin rather than
        // having the SDK threaded through every call site.
        val koin = create(module { single { Repository(get()) } }).koin

        assertSame(scopedD2, koin.get<Repository>().sdk)
    }

    @Test
    fun `give a plugin with no module a usable container anyway`() {
        // A plugin that declares no bindings still gets the seeded ones, so `koinInject` works
        // without forcing every author to write an empty module.
        assertNotNull(create(pluginModule = null).koin.get<ScopedD2>())
    }

    @Test
    fun `withhold the unrestricted SDK`() {
        // The reason the container is private in the first place. Loading plugin modules into the
        // application container let a plugin resolve every host binding, D2 included.
        val koin = create().koin

        assertThrows(NoDefinitionFoundException::class.java) { koin.get<D2>() }
    }

    @Test
    fun `keep separate plugins in separate containers`() {
        val otherSdk: ScopedD2 = mock()
        val other = PluginContainer.create(FakePluginContext(metadata, otherSdk), null)

        try {
            assertSame(scopedD2, create().koin.get<ScopedD2>())
            assertSame(otherSdk, other.koin.get<ScopedD2>())
        } finally {
            other.close()
        }
    }

    private class Repository(
        val sdk: ScopedD2,
    )

    private class FakePluginContext(
        override val pluginMetadata: PluginMetadata,
        override val sdk: ScopedD2,
    ) : Dhis2PluginContext {
        @Deprecated("Use sdk.trackedEntityInstances()")
        override suspend fun getTrackedEntityInstances(programUid: String) = Result.success(emptyList<TrackedEntityInstanceDto>())

        @Deprecated("Use sdk.dataValues()")
        override suspend fun getDataValues(
            orgUnitUid: String,
            dataSetUid: String,
            period: String,
        ) = Result.success(emptyList<DataValueDto>())

        @Deprecated("Use sdk.dataValues().value(...)")
        override suspend fun saveDataValue(
            dataSetUid: String,
            dataValue: DataValueDto,
        ) = Result.success(Unit)
    }
}
