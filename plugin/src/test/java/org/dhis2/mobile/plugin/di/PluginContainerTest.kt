package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.hisp.dhis.android.core.D2
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.kotlin.mock

/**
 * Specifies what a plugin's private Koin container holds.
 *
 * Note that `get<D2>()` is **expected to resolve** here. That is the whole point of this iteration:
 * the plugin is handed the SDK deliberately. The container exists for host integrity, not
 * confidentiality — plugin modules used to be loaded into the *application* container, where Koin's
 * default override meant a plugin binding could silently replace a host one for the rest of the app.
 *
 * When access narrowing lands, this assertion inverts: the container will seed a restricted handle
 * and `get<D2>()` will stop resolving. Keeping the assertion explicit either way is what makes the
 * difference between the two iterations legible.
 */
class PluginContainerTest {
    private val d2: D2 = mock()

    private val metadata =
        PluginMetadata(
            id = "org.myorg.my-plugin",
            version = "1.0.0",
            entryPoint = "org.myorg.plugin.MyPlugin",
        )

    private val context: Dhis2PluginContext = FakePluginContext(metadata, d2)

    private var container: KoinApplication? = null

    @After
    fun tearDown() {
        container?.close()
    }

    private fun create(pluginModule: Module? = null): KoinApplication =
        PluginContainer.create(context, pluginModule).also { container = it }

    @Test
    fun `hand the plugin the SDK`() {
        assertSame(d2, create().koin.get<D2>())
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
        // The reason for seeding: a plugin repository can be constructed by Koin rather than having
        // the SDK threaded through every call site.
        val koin = create(module { single { Repository(get()) } }).koin

        assertSame(d2, koin.get<Repository>().sdk)
    }

    @Test
    fun `give a plugin with no module a usable container anyway`() {
        assertNotNull(create(pluginModule = null).koin.get<D2>())
    }

    @Test
    fun `keep separate plugins in separate containers`() {
        // Isolation is per plugin, so one plugin's bindings can never be seen — or overridden — by
        // another.
        val otherD2: D2 = mock()
        val other = PluginContainer.create(FakePluginContext(metadata, otherD2), null)

        try {
            assertSame(d2, create().koin.get<D2>())
            assertSame(otherD2, other.koin.get<D2>())
        } finally {
            other.close()
        }
    }

    @Test
    fun `let a plugin binding shadow a host type without touching the host container`() {
        // The defect this container fixes: with `koin.loadModules(...)` on the application
        // container, this binding would have replaced the host's for the rest of the app.
        val koin = create(module { single { "plugin value" } }).koin

        assertEquals("plugin value", koin.get<String>())
    }

    private class Repository(
        val sdk: D2,
    )

    private class FakePluginContext(
        override val pluginMetadata: PluginMetadata,
        override val sdk: D2,
    ) : Dhis2PluginContext
}
