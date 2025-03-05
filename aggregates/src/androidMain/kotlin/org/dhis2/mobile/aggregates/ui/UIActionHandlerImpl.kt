package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

internal class UIActionHandlerImpl(private val activity: ComponentActivity) : UIActionHandler {
    private var callback: ((String?) -> Unit)? = null

    private val launcher =
        activity.activityResultRegistry.register(
            key = "UI_ACTION_LAUNCHER",
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: String? = result.data?.getStringExtra("result_key")
                callback?.invoke(data)
            } else {
                callback?.invoke(null)
            }
        }

    override fun onCaptureCoordinates(
        fieldUid: String,
        locationType: String,
        initialData: String,
        programUid: String,
        callback: (result: String?) -> Unit,
    ) {
    }
}
