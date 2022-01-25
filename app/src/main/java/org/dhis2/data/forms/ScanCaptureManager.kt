package org.dhis2.data.forms

import android.app.Activity
import android.content.Intent
import com.google.zxing.ResultMetadataType
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

const val SCAN_SYMBOLOGY_ID = "SCAN_SYMBOLOGY_ID"

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
                intent.putExtra(SCAN_SYMBOLOGY_ID, it.toString())
                intent.putExtra(
                    Intents.Scan.RESULT,
                    it.toString() + intent.getStringExtra(Intents.Scan.RESULT)
                )
            }
            return intent
        }
    }
}