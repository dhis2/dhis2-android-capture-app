package org.dhis2.mobile.myplugin

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import org.dhis2.commons.plugin.PluginInterface

class PluginImpl : PluginInterface, ComponentActivity() {
    private val getActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    @Composable
    override fun Show(context: Context) {
        Intent(context, MainActivity::class.java).apply {
            getActivityContent.launch(this)
        }
    }

    private fun initializeMainActivity() {

    }
}
