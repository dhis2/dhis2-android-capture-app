package org.dhis2.mobile.plugin.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dalvik.system.InMemoryDexClassLoader
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

/**
 * Loads a [Dhis2Plugin] from a signed zip bundle.
 *
 * Bundle layout (see the sample project's `buildPluginBundle` task):
 *
 * ```
 * {id}-{version}.zip
 * ├── META-INF/…                 (jarsigner; verified separately by PluginVerifier)
 * ├── plugin.json                (metadata manifest — informational, not read here)
 * └── android/
 *     ├── classes.dex            (loaded via InMemoryDexClassLoader)
 *     └── composeResources/…     (compose multiplatform resources)
 * ```
 *
 * On load, the bundle is unzipped into `{bundleZip.parentFile}/{id}-{version}/`.
 * The DEX is loaded via [InMemoryDexClassLoader] (requires API 26+). DEX bytes are
 * wrapped in a [ByteBuffer] and never re-written to disk, satisfying Android 10+
 * W^X policy.
 *
 * The extracted `android/` directory is returned as [LoadedPlugin.resourceRoot].
 * The host's `PluginSlot` Composable installs a filesystem-backed `ResourceReader`
 * pointing at that directory via `CompositionLocalProvider(LocalResourceReader …)`,
 * so the plugin's CMP Resources (`Res.string.foo`, `painterResource(Res.drawable.foo)`)
 * resolve from the extracted files without going through Android's AssetManager.
 */
class PluginLoader(private val context: Context) {

    @Suppress("UnusedPrivateProperty")
    private val hostContext = context

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(bundleZip: File, metadata: PluginMetadata): LoadedPlugin {
        val targetDir = File(bundleZip.parentFile, "${metadata.id}-${metadata.version}").apply {
            deleteRecursively()
            mkdirs()
        }

        ZipFile(bundleZip).use { zip ->
            zip.entries().asSequence().filterNot { it.isDirectory }.forEach { entry ->
                val outFile = File(targetDir, entry.name).apply { parentFile?.mkdirs() }
                zip.getInputStream(entry).use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }

        val androidRoot = File(targetDir, "android")
        val dexFile = File(androidRoot, "classes.dex")
        check(dexFile.exists()) {
            "Plugin bundle '${metadata.id}' is missing android/classes.dex"
        }

        val dexBytes = dexFile.readBytes()
        val classLoader = InMemoryDexClassLoader(
            ByteBuffer.wrap(dexBytes),
            PluginLoader::class.java.classLoader,
        )

        Timber.d(
            "Loading plugin '${metadata.id}' v${metadata.version} from DEX " +
                "(${dexBytes.size} bytes) with resource root ${androidRoot.absolutePath}",
        )

        val pluginClass = classLoader.loadClass(metadata.entryPoint)
        require(Dhis2Plugin::class.java.isAssignableFrom(pluginClass)) {
            "Plugin entry point '${metadata.entryPoint}' does not implement Dhis2Plugin"
        }

        @Suppress("UNCHECKED_CAST")
        val plugin = (pluginClass as Class<out Dhis2Plugin>)
            .getDeclaredConstructor()
            .newInstance()

        return LoadedPlugin(plugin = plugin, resourceRoot = androidRoot)
    }
}

/** A loaded plugin paired with its extracted resource root (the `android/` directory). */
data class LoadedPlugin(
    val plugin: Dhis2Plugin,
    val resourceRoot: File,
)
