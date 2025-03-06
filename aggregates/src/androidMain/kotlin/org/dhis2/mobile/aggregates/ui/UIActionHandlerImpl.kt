package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.maps.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.INITIAL_GEOMETRY_COORDINATES
import org.dhis2.maps.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.PROGRAM_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.SCOPE

internal class UIActionHandlerImpl(
    private val context: FragmentActivity,
    private val dataSetUid: String,
) : UIActionHandler {
    private var callback: ((String?) -> Unit)? = null

    private val mapLauncher =
        context.activityResultRegistry.register(
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
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        val intent = Intent(context, MapSelectorActivity::class.java)
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType)
        intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
        intent.putExtra(PROGRAM_UID, dataSetUid)
        intent.putExtra(SCOPE, MapScope.DATA_SET.name)
        mapLauncher.launch(intent)
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
