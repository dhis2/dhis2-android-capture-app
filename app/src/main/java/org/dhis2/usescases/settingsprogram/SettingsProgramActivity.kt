package org.dhis2.usescases.settingsprogram

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.settingsprogram.ui.SettingsProgramScreen
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

class SettingsProgramActivity : ActivityGlobalAbstract() {
    override var handleEdgeToEdge = false

    companion object {
        fun getIntentActivity(context: Context): Intent = Intent(context, SettingsProgramActivity::class.java)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            DHIS2Theme {
                SettingsProgramScreen(
                    onBack = onBackPressedDispatcher::onBackPressed,
                )
            }
        }
    }
}
