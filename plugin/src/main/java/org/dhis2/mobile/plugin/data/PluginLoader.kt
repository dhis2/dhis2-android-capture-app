package org.dhis2.mobile.plugin.data

import android.os.Build
import androidx.annotation.RequiresApi
import dalvik.system.InMemoryDexClassLoader
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * Loads a [Dhis2Plugin] from a raw DEX byte array using [InMemoryDexClassLoader].
 *
 * The DEX bytes are never written to disk during loading, which satisfies the Android 10+ (API 29)
 * W^X (write-xor-execute) policy. [InMemoryDexClassLoader] requires API 26+.
 *
 * The plugin class is resolved using [PluginMetadata.entryPoint]. It must:
 * - Implement [Dhis2Plugin]
 * - Have a public no-argument constructor
 * - Be compiled against (not bundle) the `:plugin-sdk` artifact
 *
 * Because the parent class loader is the host app's class loader, the plugin can resolve
 * all `:plugin-sdk` types at runtime without bundling them in the DEX.
 */
class PluginLoader {
    @RequiresApi(Build.VERSION_CODES.O)
    fun load(pluginBytes: ByteArray, metadata: PluginMetadata): Dhis2Plugin {
        val buffer = ByteBuffer.wrap(pluginBytes)
        val classLoader = InMemoryDexClassLoader(
            buffer,
            PluginLoader::class.java.classLoader,
        )

        Timber.d("Loading plugin '${metadata.id}' v${metadata.version} from DEX (${pluginBytes.size} bytes)")

        val pluginClass = classLoader.loadClass(metadata.entryPoint)

        require(Dhis2Plugin::class.java.isAssignableFrom(pluginClass)) {
            "Plugin entry point '${metadata.entryPoint}' does not implement Dhis2Plugin"
        }

        @Suppress("UNCHECKED_CAST")
        return (pluginClass as Class<out Dhis2Plugin>)
            .getDeclaredConstructor()
            .newInstance()
    }
}
