package org.dhis2.data.appinspector

import android.content.Context
import android.os.Build
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import org.dhis2.BuildConfig

class AppInspector(private val context: Context) {
    var flipperInterceptor: FlipperOkhttpInterceptor? = null
        private set

    fun init(): AppInspector {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
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

    private fun crashPlugin() = CrashReporterPlugin.getInstance()
}
