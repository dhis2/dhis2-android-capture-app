package org.dhis2.mobile.myplugin

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.commons.plugin.PluginInterface

class PluginImpl : PluginInterface {
    @Composable
    override fun Show() {
        Text(
            text = "This is my amazing plugin!!!",
            modifier = Modifier
        )
    }
}