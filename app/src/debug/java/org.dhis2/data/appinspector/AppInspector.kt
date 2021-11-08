package org.dhis2.data.appinspector

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.leakcanary.LeakCanaryFlipperPlugin
import com.facebook.flipper.plugins.leakcanary.RecordLeakService
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.squareup.leakcanary.LeakCanary
import org.dhis2.Bindings.app
import org.dhis2.BuildConfig

class AppInspector(private val context: Context) {
    var flipperInterceptor: FlipperOkhttpInterceptor? = null
        private set

    fun init(): AppInspector {
        SoLoader.init(context, false)
        if (BuildConfig.DEBUG && BuildConfig.FLAVOR != "dhisUITesting") {
            AndroidFlipperClient.getInstance(context).apply {
                addPlugin(
                    layoutInspectorPlugin()
                )
                addPlugin(
                    databaseInspectorPlugin()
                )
                addPlugin(
                    networkInspectorPlugin()
                )
                addPlugin(
                    sharedPreferencesPlugin()
                )
                addPlugin(
                    crashPlugin()
                )
                start()
            }
        }
        return this
    }

    private fun layoutInspectorPlugin() =
        InspectorFlipperPlugin(context, DescriptorMapping.withDefaults())

    private fun databaseInspectorPlugin() = DatabasesFlipperPlugin(context)
    private fun networkInspectorPlugin() = NetworkFlipperPlugin().also {
        flipperInterceptor = FlipperOkhttpInterceptor(it)
    }

    private fun sharedPreferencesPlugin() = SharedPreferencesFlipperPlugin(context)
    private fun leakCanaryPlugin() = LeakCanaryFlipperPlugin().also {
        LeakCanary.install(context.app())
        LeakCanary.refWatcher(context)
            .listenerServiceClass(RecordLeakService::class.java)
            .buildAndInstall()
    }

    private fun crashPlugin() = CrashReporterPlugin.getInstance()
}
