package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.maps.views.MapSelectorActivity

internal class UIActionHandlerImpl(
    private val activity: FragmentActivity,
    private val dataSetUid: String,
) : UIActionHandler {
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
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        val intent = Intent(activity, MapSelectorActivity::class.java)
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType)
        intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
        intent.putExtra(PROGRAM_UID, programUid)
        launcher.launch(intent)
    }

    companion object {
        const val FIELD_UID = "FIELD_UID_EXTRA"
        const val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        const val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"
        const val PROGRAM_UID = "PROGRAM_UID_EXTRA"
    }

    override fun onCaptureOrgUnit(
        preselectedOrgUnits: List<String>,
        callback: (result: String?) -> Unit,
    ) {
        OUTreeFragment.Builder()
            .withPreselectedOrgUnits(preselectedOrgUnits)
            .singleSelection()
            .onSelection { selectedOrgUnits ->
                callback(selectedOrgUnits.firstOrNull()?.uid())
            }
            .orgUnitScope(OrgUnitSelectorScope.DataSetCaptureScope(dataSetUid))
            .build()
            .show(activity.supportFragmentManager, dataSetUid)
    }
}
