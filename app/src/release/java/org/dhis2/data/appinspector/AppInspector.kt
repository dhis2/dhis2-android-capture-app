package org.dhis2.data.appinspector

import android.content.Context
import okhttp3.Interceptor

class AppInspector(private val context: Context) {
    var flipperInterceptor: Interceptor? = null

    fun init(): AppInspector {
        return this
    }
}
