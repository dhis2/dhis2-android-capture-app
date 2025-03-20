package org.dhis2.mobile.aggregates.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.mobile.commons.files.GetFileResource
import org.dhis2.mobile.commons.files.toFile
import java.io.File

class UiActionHandlerImpl(
    private val context: FragmentActivity,
    private val dataSetUid: String,
    private val fileHandler: FileHandler,
) : UiActionHandler {
    private var callback: ((String?) -> Unit)? = null
    private var filepath: String? = null

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

    private val fileLauncher =
        context.activityResultRegistry.register(
            key = "FILE_LAUNCHER",
            contract = GetFileResource(),
        ) { uris ->
            callback?.invoke(uris.firstOrNull()?.toFile(context = context)?.path)
        }

    private val requestStoragePermissionLauncher =
        context.activityResultRegistry.register(
            key = "STORAGE_PERMISSION_LAUNCHER",
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                filepath?.let {
                    downloadFile(it)
                }
                filepath = null
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
                if (selectedOrgUnits.isNotEmpty()) {
                    callback(selectedOrgUnits.firstOrNull()?.uid())
                }
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

    override fun onSelectFile(fieldUid: String, callback: (result: String?) -> Unit) {
        this.callback = callback
        fileLauncher.launch("*/*")
    }

    override fun onOpenFile(
        fieldUid: String,
        filepath: String?,
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            filepath?.let { downloadFile(it) } ?: callback(null)
        } else {
            this.filepath = filepath
            requestStoragePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun downloadFile(filepath: String) {
        fileHandler.copyAndOpen(File(filepath)) {
            callback?.invoke(CallbackStatus.OK.name)
        }
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
