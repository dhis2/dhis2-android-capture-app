package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope

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
