package org.dhis2.commons.locationprovider

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.commons.R

object LocationSettingLauncher {
    fun requestEnableLocationSetting(
        activityContext: Context,
        onAccept: (() -> Unit)? = null,
        onCancel: () -> Unit = {}
    ) {
        MaterialAlertDialogBuilder(activityContext, R.style.MaterialDialog)
            .setMessage(activityContext.getString(R.string.enable_location_message))
            .setPositiveButton(activityContext.getString(R.string.action_ok)) { _, _ ->
                onAccept?.invoke() ?: onAccept(activityContext)
            }
            .setNegativeButton(activityContext.getString(R.string.cancel)) { _, _ -> onCancel() }
            .show()
    }

    private fun onAccept(activityContext: Context) {
        activityContext.startActivity(locationSourceSettingIntent())
    }

    fun locationSourceSettingIntent() = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
}
