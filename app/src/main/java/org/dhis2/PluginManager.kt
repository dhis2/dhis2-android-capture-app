package org.dhis2

import android.content.Context
import dalvik.system.DexClassLoader
import org.dhis2.commons.plugin.PluginInterface
import java.io.File

class PluginManager(private val context: Context) {

    fun loadPlugin(pluginFile: File): PluginInterface? {
        val dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE).absolutePath
        val classLoader = DexClassLoader(
            pluginFile.absolutePath,
            dexOutputDir,
            null,
            context.classLoader,
        )

        return try {
            // Load the plugin class
            val pluginClass = classLoader.loadClass("org.dhis2.mobile.myplugin.PluginImpl")
            val pluginInstance =
                pluginClass.getDeclaredConstructor().newInstance() as PluginInterface
            pluginInstance
        } catch (e: Exception) {
            null
        }
    }
}
