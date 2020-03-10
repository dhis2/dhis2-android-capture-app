package org.dhis2.usescases.qrScanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityQrBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import javax.inject.Inject

class ScanActivity : ActivityGlobalAbstract(), ZXingScannerView.ResultHandler {
    private lateinit var binding: ActivityQrBinding
    private lateinit var mScannerView: ZXingScannerView
    private var isPermissionRequested = false
    private var optionSetUid: String? = null

    @Inject
    lateinit var scanPresenter: ScanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        optionSetUid = intent.getStringExtra(Constants.OPTION_SET)
        (applicationContext as App)
            .userComponent()
            ?.plus(ScanModule(optionSetUid))
            ?.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr)
        mScannerView = binding.scannerView
        mScannerView.setAutoFocus(true)
        mScannerView.setFormats(ZXingScannerView.ALL_FORMATS)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initScanner()
        } else if (!isPermissionRequested) {
            isPermissionRequested = true
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        } else {
            abstractActivity.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    private fun initScanner() {
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    initScanner()
                } else {
                    finish()
                }
            }
        }
    }

    override fun handleResult(result: Result) {
        if(optionSetUid == null ||
            scanPresenter.getOptions().any { it.displayName() == result.text }
        ) {
            val url = result.text
            val data = Intent()
            data.putExtra(Constants.EXTRA_DATA, url)
            setResult(Activity.RESULT_OK, data)
        }
        finish()
    }
}