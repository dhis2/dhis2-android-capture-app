package org.dhis2.usescases.qrScanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.databinding.DataBindingUtil
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.databinding.ActivityScanBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import javax.inject.Inject

class ScanActivity : ActivityGlobalAbstract() {
    private lateinit var binding: ActivityScanBinding
    private lateinit var mScannerView: DecoratedBarcodeView
    private lateinit var capture: CaptureManager
    private var uid: String? = null
    private var optionSetUid: String? = null
    private var renderingType: ValueTypeRenderingType? = null

    @Inject
    lateinit var scanRepository: ScanRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = intent.getStringExtra(Constants.UID)
        optionSetUid = intent.getStringExtra(Constants.OPTION_SET)
        (applicationContext as App)
            .userComponent()
            ?.plus(ScanModule(optionSetUid))
            ?.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan)
        renderingType =
            intent.getSerializableExtra(Constants.SCAN_RENDERING_TYPE) as ValueTypeRenderingType?
        mScannerView = binding.scannerView
        capture = CaptureManager(this, mScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
        initScannerCallback()
    }

    private fun initScannerCallback() {
        mScannerView.decodeSingle { barcodeResult ->
            var url = barcodeResult.text

            if (optionSetUid != null) {
                val option = scanRepository.getOptions()
                    .firstOrNull {
                        it.displayName() == barcodeResult.text ||
                            it.name() == barcodeResult.text ||
                            it.code() == barcodeResult.text
                    }
                if (option != null) {
                    url = option.displayName()
                } else {
                    finish()
                }
            }

            val data = Intent()
            data.putExtra(Constants.UID, uid)
            data.putExtra(Constants.EXTRA_DATA, url)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
