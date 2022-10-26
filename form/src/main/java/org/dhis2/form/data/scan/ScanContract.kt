package org.dhis2.form.data.scan

import android.content.Context
import android.content.Intent
import com.journeyapps.barcodescanner.ScanContract as DefaultScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class ScanContract : DefaultScanContract() {

    private lateinit var fieldUid: String
    private var optionSetUid: String? = null

    override fun createIntent(context: Context, input: ScanOptions?): Intent {
        input?.let {
            this.fieldUid = it.moreExtras?.get(UID) as String
            it.moreExtras?.getOrDefault(OPTION_SET, null)?.let { uid ->
                this.optionSetUid = uid as String
            }
        }
        return context.scanCaptureActivityIntent(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        intent?.apply {
            putExtra(UID, fieldUid)
            putExtra(OPTION_SET, optionSetUid)
        }
        return super.parseResult(resultCode, intent)
    }

    companion object {
        const val UID = "UID"
        const val OPTION_SET = "OPTION_SET"
    }
}
