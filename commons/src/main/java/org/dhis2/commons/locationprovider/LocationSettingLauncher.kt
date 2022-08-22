package org.dhis2.commons.locationprovider

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import org.dhis2.commons.R

object LocationSettingLauncher {
    fun requestEnableLocationSetting(activityContext: Context, onCancel: () -> Unit = {}) {
        AlertDialog.Builder(activityContext, R.style.CustomDialog)
            .setMessage(activityContext.getString(R.string.enable_location_message))
            .setPositiveButton(
                activityContext.getString(R.string.action_ok)
            ) { _, _ ->
                activityContext.startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton(activityContext.getString(R.string.cancel)) { _, _ -> onCancel() }
            .show()
    }
}
