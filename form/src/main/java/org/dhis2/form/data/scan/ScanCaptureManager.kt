package org.dhis2.form.data.scan

import android.app.Activity
import android.content.Intent
import com.google.zxing.ResultMetadataType
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

const val SCAN_SYMBOLOGY_ID = "SCAN_SYMBOLOGY_ID"
const val GS1_IDENTIFIER = "]d2"

class ScanCaptureManager(val activity: Activity, barcodeView: DecoratedBarcodeView) :
    CaptureManager(activity, barcodeView) {

    override fun returnResult(rawResult: BarcodeResult?) {
        val intent = resultIntent(rawResult, null)
        activity.setResult(Activity.RESULT_OK, intent)
        closeAndFinish()
    }

    companion object {
        fun resultIntent(rawResult: BarcodeResult?, barcodeImagePath: String?): Intent {
            val intent = CaptureManager.resultIntent(rawResult, barcodeImagePath)
            rawResult?.resultMetadata?.get(ResultMetadataType.SYMBOLOGY_IDENTIFIER)?.let {
                val result = if (it == GS1_IDENTIFIER) {
                    intent.putExtra(SCAN_SYMBOLOGY_ID, it.toString())
                    it.toString() + intent.getStringExtra(Intents.Scan.RESULT)
                } else {
                    intent.getStringExtra(Intents.Scan.RESULT)
                }
                intent.putExtra(
                    Intents.Scan.RESULT,
                    result
                )
            }
            return intent
        }
    }
}
