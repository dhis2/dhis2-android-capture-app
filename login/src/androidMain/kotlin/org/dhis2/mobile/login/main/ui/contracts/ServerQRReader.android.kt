package org.dhis2.mobile.login.main.ui.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable

private const val SCAN_ACTIVITY_CLASS_NAME = "org.dhis2.usescases.qrScanner.ScanActivity"

@Composable
actual fun serverQrReader(onResult: (String?) -> Unit): ServerQRReader {
    val launcher =
        rememberLauncherForActivityResult(ReflectiveScanActivityContract()) { serverUrl ->
            onResult(serverUrl)
        }

    return object : ServerQRReader {
        override fun launch() {
            launcher.launch(Unit)
        }
    }
}

internal class ReflectiveScanActivityContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(
        context: Context,
        input: Unit,
    ): Intent {
        val intent = Intent()
        intent.setClassName(context.packageName, SCAN_ACTIVITY_CLASS_NAME)
        return intent
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): String? =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra("extra_data")
        } else {
            null
        }
}
