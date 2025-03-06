package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import org.dhis2.maps.views.MapSelectorActivity

internal class UIActionHandlerImpl(private val activity: ComponentActivity) : UIActionHandler {
    private var callback: ((String?) -> Unit)? = null

    private val mapLauncher =
        activity.activityResultRegistry.register(
            key = "UI_ACTION_LAUNCHER",
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.extras?.let {
                    val uid = result.data?.getStringExtra(FIELD_UID)
                    val featureType = result.data?.getStringExtra(LOCATION_TYPE_EXTRA)
                    val coordinates = result.data?.getStringExtra(DATA_EXTRA)
                    if (uid != null && featureType != null) {
                        callback?.invoke(coordinates)
                    }
                } ?: callback?.invoke(null)
            } else {
                callback?.invoke(null)
            }
        }

    override fun onCaptureCoordinates(
        fieldUid: String,
        locationType: String,
        initialData: String?,
        programUid: String?,
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        val intent = Intent(activity, MapSelectorActivity::class.java)
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType)
        intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
        intent.putExtra(PROGRAM_UID, programUid)
        mapLauncher.launch(intent)
    }

    companion object {
        const val DATA_EXTRA = "data_extra"
        const val FIELD_UID = "FIELD_UID_EXTRA"
        const val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        const val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"
        const val PROGRAM_UID = "PROGRAM_UID_EXTRA"
    }
}
