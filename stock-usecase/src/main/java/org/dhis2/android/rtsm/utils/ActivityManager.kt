package org.dhis2.android.rtsm.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import org.dhis2.android.rtsm.R

class ActivityManager {
    companion object {
        @JvmStatic
        fun startActivity(activity: Activity, intent: Intent, closeCurrentActivity: Boolean) {
            activity.startActivity(intent)

            if (closeCurrentActivity) {
                activity.finish()
            }
        }

        @JvmStatic
        private fun showMessage(view: View, message: String, isError: Boolean) {
            val color = if (isError) {
                R.color.error
            } else {
                R.color.primary_color
            }

            if (message.isNotEmpty()) {
                Snackbar.make(view, message, LENGTH_LONG).setBackgroundTint(
                    ContextCompat.getColor(view.context, color),
                ).apply {
                    this.view.findViewById<TextView>(
                        com.google.android.material.R.id.snackbar_text,
                    )?.maxLines = 2
                }.show()
            }
        }

        @JvmStatic
        fun showErrorMessage(view: View, message: String) {
            showMessage(view, message, true)
        }

        @JvmStatic
        fun showInfoMessage(view: View, message: String) {
            showMessage(view, message, false)
        }

        @JvmStatic
        fun showToast(context: Context, messageRes: Int) {
            showToast(context, context.getString(messageRes))
        }

        @JvmStatic
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JvmStatic
        fun hasFlash(context: Context): Boolean {
            return context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        }

        @JvmStatic
        fun showDialog(
            context: Context,
            titleRes: Int,
            messageRes: String,
            confirmationCallback: () -> Unit,
        ) {
            MaterialAlertDialogBuilder(context, R.style.MaterialDialog)
                .setMessage(messageRes)
                .setTitle(titleRes)
                .setPositiveButton(android.R.string.ok) { _, _ -> confirmationCallback() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
        }

        @JvmStatic
        fun checkPermission(activity: Activity, requestCode: Int) {
            if (ContextCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.RECORD_AUDIO,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    requestCode,
                )
            }
        }
    }
}
