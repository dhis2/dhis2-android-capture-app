package org.dhis2.tracker.ui

import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.dhis2.commons.Constants
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class UiActionHandlerImpl(
    private val context: AppCompatActivity,
) : UiActionHandler {

    private var qrScanCallback: ((String?) -> Unit)? = null

    private val qrScanLauncher = context.activityResultRegistry.register(
        "qrScanLauncher",
        ScanContract(),
    ) { result ->
        result.contents?.let { qrData ->
            qrScanCallback?.invoke(qrData)
        }
        qrScanCallback = null
    }

    override fun onCaptureCoordinates(
        fieldUid: String,
        locationType: String,
        initialData: String?,
        callback: (String?) -> Unit,
    ) {
        // Not supported on search
    }

    override fun onCaptureOrgUnit(
        preselectedOrgUnits: List<String>,
        callback: (String?) -> Unit,
    ) {
        OUTreeFragment.Builder()
            .withPreselectedOrgUnits(preselectedOrgUnits)
            .singleSelection()
            .onSelection { selectedOrgUnits: List<OrganisationUnit> ->
                if (selectedOrgUnits.isNotEmpty()) {
                    callback(selectedOrgUnits.firstOrNull()?.uid())
                }
            }
            .orgUnitScope(
                orgUnitScope = OrgUnitSelectorScope.UserSearchScope(),
            )
            .build()
            .show(context.supportFragmentManager, "OUTreeFragment")
    }

    override fun onCall(phoneNumber: String, onActivityNotFound: () -> Unit) {
        // Not supported on search
    }

    override fun onSendEmail(email: String, onActivityNotFound: () -> Unit) {
        // Not supported on search
    }

    override fun onOpenLink(url: String, onActivityNotFound: () -> Unit) {
        // Not supported on search
    }

    override fun onSelectFile(
        fieldUid: String,
        callback: (String?) -> Unit,
        onFailure: () -> Unit,
    ) {
        // Not supported on search
    }

    override fun onDownloadFile(
        fieldUid: String,
        filepath: String?,
        callback: (String?) -> Unit,
    ) {
        // Not supported on search
    }

    override fun onAddImage(fieldUid: String, callback: (String?) -> Unit) {
        // Not supported on search
    }

    override fun onTakePicture(callback: (String?) -> Unit) {
        // Not supported on search
    }

    override fun onShareImage(filepath: String?, onActivityNotFound: () -> Unit) {
        // Not supported on search
    }

    override fun onQRScan(
        fieldUid: String,
        optionSet: String?,
        callback: (String?) -> Unit,
    ) {
        qrScanCallback = callback

        qrScanLauncher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats()
                setPrompt("Scan a QR code")
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                addExtra(Constants.UID, fieldUid)
                optionSet?.let {
                    addExtra(
                        Constants.OPTION_SET,
                        optionSet,
                    )
                }
            },
        )
    }

    override fun onDisplayQRCode(
        fieldUid: String,
        value: String,
        label: String,
        editable: Boolean,
        onScan: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override fun onBarcodeScan(
        fieldUid: String,
        optionSet: String?,
        callback: (String?) -> Unit,
    ) {
        qrScanCallback = callback

        qrScanLauncher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats()
                setPrompt("Scan a barcode")
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                addExtra(Constants.UID, fieldUid)
                optionSet?.let {
                    addExtra(
                        Constants.OPTION_SET,
                        optionSet,
                    )
                }
            },
        )
    }

    override fun onDisplayBarCode(
        fieldUid: String,
        value: String,
        label: String,
        editable: Boolean,
        onScan: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }
}
