package org.dhis2.mobile.aggregates.data.files

import android.content.Context

object AggregatesFileProvider {
    lateinit var fileProviderAuthority: String

    fun init(context: Context) {
        fileProviderAuthority = "${context.packageName}.aggregates.data.AggregatesFileProvider"
    }
}
