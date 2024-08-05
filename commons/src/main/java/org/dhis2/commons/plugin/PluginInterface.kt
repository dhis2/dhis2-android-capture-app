package org.dhis2.commons.plugin

import android.content.Context
import androidx.compose.runtime.Composable

interface PluginInterface {
    @Composable
    fun Show(Context: Context)
}
