package org.dhis2.mobile.aggregates.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.maps.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.INITIAL_GEOMETRY_COORDINATES
import org.dhis2.maps.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.PROGRAM_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.SCOPE
import org.dhis2.mobile.aggregates.R
import org.dhis2.mobile.aggregates.data.files.AggregatesFileProvider
import org.dhis2.mobile.commons.extensions.rotateImage
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.mobile.commons.files.GetFileResource
import org.dhis2.mobile.commons.files.toFileOverWrite
import org.dhis2.mobile.commons.input.CallbackStatus
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import java.io.File

class UiActionHandlerImpl(
    private val context: FragmentActivity,
    private val dataSetUid: String,
    private val fileHandler: FileHandler,
) : UiActionHandler {
    private var callback: ((String?) -> Unit)? = null
    private var onFailure: (() -> Unit)? = null
    private var filepath: String? = null
    private var tempFile: File? = null

    init {
        AggregatesFileProvider.init(context.applicationContext)
    }

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
            if (uris.isNotEmpty()) {
                callback?.invoke(uris.firstOrNull()?.toFileOverWrite(context = context)?.path)
            } else {
                onFailure?.invoke()
            }
        }

    private val cameraLauncher =
        context.activityResultRegistry.register(
            key = "CAMERA_LAUNCHER",
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                tempFile?.let {
                    callback?.invoke(it.rotateImage(context).path)
                } ?: run {
                    callback?.invoke(CallbackStatus.ERROR.name)
                }
                tempFile = null
            } else {
                callback?.invoke(CallbackStatus.ERROR.name)
            }
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

    private val requestCameraPermissionLauncher =
        context.activityResultRegistry.register(
            key = "CAMERA_PERMISSION_LAUNCHER",
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(getPhotoUri(tempFile!!))
            } else {
                callback?.invoke(CallbackStatus.ERROR.name)
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
        OUTreeFragment
            .Builder()
            .withPreselectedOrgUnits(preselectedOrgUnits)
            .singleSelection()
            .onSelection { selectedOrgUnits ->
                if (selectedOrgUnits.isNotEmpty()) {
                    callback(selectedOrgUnits.firstOrNull()?.uid())
                }
            }.orgUnitScope(OrgUnitSelectorScope.DataSetCaptureScope(dataSetUid))
            .build()
            .show(context.supportFragmentManager, dataSetUid)
    }

    override fun onCall(
        phoneNumber: String,
        onActivityNotFound: () -> Unit,
    ) {
        val phoneCallIntent =
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    override fun onSendEmail(
        email: String,
        onActivityNotFound: () -> Unit,
    ) {
        val phoneCallIntent =
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    override fun onOpenLink(
        url: String,
        onActivityNotFound: () -> Unit,
    ) {
        val phoneCallIntent =
            Intent(Intent.ACTION_VIEW).apply {
                data =
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        Uri.parse("http://$url")
                    } else {
                        Uri.parse(url)
                    }
            }
        launchIntentChooser(phoneCallIntent, onActivityNotFound)
    }

    override fun onSelectFile(
        fieldUid: String,
        callback: (result: String?) -> Unit,
        onFailure: () -> Unit,
    ) {
        this.callback = callback
        this.onFailure = onFailure
        fileLauncher.launch("*/*")
    }

    override fun onDownloadFile(
        fieldUid: String,
        filepath: String?,
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            filepath?.let { downloadFile(it) } ?: callback(null)
        } else {
            this.filepath = filepath
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onAddImage(
        fieldUid: String,
        callback: (result: String?) -> Unit,
    ) {
        this.callback = callback
        fileLauncher.launch("image/*")
    }

    override fun onTakePicture(callback: (result: String?) -> Unit) {
        this.callback = callback
        tempFile = getTempFile()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(getPhotoUri(tempFile!!))
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onShareImage(
        filepath: String?,
        onActivityNotFound: () -> Unit,
    ) {
        filepath?.let {
            val contentUri =
                FileProvider.getUriForFile(
                    context,
                    AggregatesFileProvider.fileProviderAuthority,
                    File(it),
                )
            val shareImageIntent =
                Intent(ACTION_SEND).apply {
                    setDataAndType(
                        contentUri,
                        context.contentResolver.getType(contentUri),
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                }

            launchIntentChooser(shareImageIntent, onActivityNotFound)
        }
    }

    override fun onQRScan(
        fieldUid: String,
        optionSet: String?,
        callback: (String?) -> Unit,
    ) {
        // Not implemented, because dataset does not support rendering types.
    }

    override fun onBarcodeScan(
        fieldUid: String,
        optionSet: String?,
        callback: (String?) -> Unit,
    ) {
        // Not implemented, because dataset does not support rendering types.
    }

    private fun downloadFile(filepath: String) {
        fileHandler.copyAndOpen(File(filepath)) {
            callback?.invoke(CallbackStatus.OK.name)
        }
    }

    private fun launchIntentChooser(
        intent: Intent,
        onActivityNotFound: () -> Unit,
    ) {
        val title = context.getString(R.string.open_with)
        val chooser = Intent.createChooser(intent, title)

        try {
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            onActivityNotFound()
        }
    }

    private fun getTempFile() = File(FileResourceDirectoryHelper.getFileResourceDirectory(context), "tempFile.png")

    private fun getPhotoUri(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            AggregatesFileProvider.fileProviderAuthority,
            file,
        )
}
