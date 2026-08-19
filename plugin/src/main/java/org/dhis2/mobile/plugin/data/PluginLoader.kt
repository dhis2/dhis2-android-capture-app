package org.dhis2.mobile.plugin.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dalvik.system.InMemoryDexClassLoader
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.security.FilteringClassLoader
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

/**
 * Loads a [Dhis2Plugin] from a signed zip bundle.
 *
 * Bundle layout, as produced by `buildPluginBundle` from the `:plugin-sdk-gradle` plugin:
 *
 * ```
 * {module}-{version}.zip
 * ├── META-INF/…                 (JAR signature; verified separately by PluginVerifier)
 * └── android/
 *     ├── classes.dex            (loaded via InMemoryDexClassLoader)
 *     └── composeResources/…     (compose multiplatform resources)
 * ```
 *
 * The bundle carries no manifest of its own: identity, entry point and data scope all come from
 * the server config ([PluginMetadata]), so the filename is immaterial to loading.
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
class PluginLoader(
    private val context: Context,
) {
    @Suppress("UnusedPrivateProperty")
    private val hostContext = context

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(
        bundleZip: File,
        metadata: PluginMetadata,
    ): LoadedPlugin {
        val targetDir =
            File(bundleZip.parentFile, "${metadata.id}-${metadata.version}").apply {
                deleteRecursively()
                mkdirs()
            }

        ZipFile(bundleZip).use { zip ->
            zip.entries().asSequence().filterNot { it.isDirectory }.forEach { entry ->
                val outFile = resolveWithin(targetDir, entry.name).apply { parentFile?.mkdirs() }
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
        // The parent is a filtering loader rather than the host's directly, so the plugin cannot
        // name D2Manager, Koin's GlobalContext or the host's own classes. See
        // PluginClassLoaderPolicy for what this does and does not achieve.
        val classLoader =
            InMemoryDexClassLoader(
                ByteBuffer.wrap(dexBytes),
                FilteringClassLoader(checkNotNull(PluginLoader::class.java.classLoader)),
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
        val plugin =
            (pluginClass as Class<out Dhis2Plugin>)
                .getDeclaredConstructor()
                .newInstance()

        return LoadedPlugin(
            plugin = plugin,
            resourceRoot = androidRoot,
            classLoader = classLoader,
        )
    }

    /**
     * Resolves [entryName] under [targetDir], refusing anything that escapes it.
     *
     * Zip entry names are attacker-controlled — and stay attacker-controlled inside a *validly
     * signed* bundle, since the signature attests to who built the zip, not to what is in it. An
     * entry named `../../databases/dhis.db` would otherwise be written wherever it pointed.
     */
    private fun resolveWithin(
        targetDir: File,
        entryName: String,
    ): File {
        val target = File(targetDir, entryName)
        val canonicalDir = targetDir.canonicalPath + File.separator
        require(target.canonicalPath.startsWith(canonicalDir)) {
            "Plugin bundle entry '$entryName' resolves outside the extraction directory"
        }
        return target
    }
}

/**
 * A loaded plugin paired with its extracted resource root (the `android/` directory) and the class
 * loader it was loaded with.
 *
 * The loader is carried so the render path can hand the plugin a [android.content.Context] whose
 * `getClassLoader()` returns *this* loader rather than the app's — otherwise
 * `LocalContext.current.classLoader` would give straight back the unfiltered host loader.
 */
data class LoadedPlugin(
    val plugin: Dhis2Plugin,
    val resourceRoot: File,
    val classLoader: ClassLoader,
)
