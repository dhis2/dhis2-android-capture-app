package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import org.dhis2.mobile.aggregates.R

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
            .show(context.supportFragmentManager, dataSetUid)
    }

    override fun onCall(phoneNumber: String, onActivityNotFound: () -> Unit) {
        val phoneCallIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    override fun onSendEmail(email: String, onActivityNotFound: () -> Unit) {
        val phoneCallIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    override fun onOpenLink(url: String, onActivityNotFound: () -> Unit) {
        val phoneCallIntent = Intent(Intent.ACTION_VIEW).apply {
            data =
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Uri.parse("http://$url")
                } else {
                    Uri.parse(url)
                }
        }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    private fun launchIntentChooser(intent: Intent, onActivityNotFound: () -> Unit) {
        val title = context.getString(R.string.open_with)
        val chooser = Intent.createChooser(intent, title)

        try {
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            onActivityNotFound()
        }
    }
}
