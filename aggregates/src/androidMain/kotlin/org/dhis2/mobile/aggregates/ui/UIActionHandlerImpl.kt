package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import org.dhis2.mobile.aggregates.R
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

    override fun onCall(phoneNumber: String) {
        val phoneCallIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        launchIntentChooser(phoneCallIntent)
    }

    override fun onSendEmail(email: String) {
        val phoneCallIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        launchIntentChooser(phoneCallIntent)
    }

    override fun onOpenLink(url: String) {
        val phoneCallIntent = Intent(Intent.ACTION_DIAL).apply {
            data =
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Uri.parse("http://$url")
                } else {
                    Uri.parse(url)
                }
        }
        launchIntentChooser(phoneCallIntent)
    }

    private fun launchIntentChooser(intent: Intent) {
        val title = activity.getString(R.string.open_with)
        val chooser = Intent.createChooser(intent, title)

        try {
            activity.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            /*do nothing*/
        }
    }
}
