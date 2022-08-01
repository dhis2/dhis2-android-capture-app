package org.dhis2.data.forms

import android.content.Context
import android.content.Intent
import com.journeyapps.barcodescanner.ScanContract as DefaultScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.dhis2.utils.Constants

class ScanContract() : DefaultScanContract() {

    private lateinit var fieldUid: String
    private var optionSetUid: String? = null

    override fun createIntent(context: Context, input: ScanOptions?): Intent {
        input?.let {
            this.fieldUid = it.moreExtras?.get(Constants.UID) as String
            it.moreExtras?.getOrDefault(Constants.OPTION_SET, null)?.let { uid ->
                this.optionSetUid = uid as String
            }
        }
        return context.scanCaptureActivityIntent(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        intent?.apply {
            putExtra(Constants.UID, fieldUid)
            putExtra(Constants.OPTION_SET, optionSetUid)
        }
        return super.parseResult(resultCode, intent)
    }
}
