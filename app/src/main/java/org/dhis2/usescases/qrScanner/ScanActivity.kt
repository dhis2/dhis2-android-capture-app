package org.dhis2.usescases.qrScanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.zxing.BarcodeFormat.AZTEC
import com.google.zxing.BarcodeFormat.CODABAR
import com.google.zxing.BarcodeFormat.CODE_128
import com.google.zxing.BarcodeFormat.CODE_39
import com.google.zxing.BarcodeFormat.CODE_93
import com.google.zxing.BarcodeFormat.DATA_MATRIX
import com.google.zxing.BarcodeFormat.EAN_13
import com.google.zxing.BarcodeFormat.EAN_8
import com.google.zxing.BarcodeFormat.ITF
import com.google.zxing.BarcodeFormat.MAXICODE
import com.google.zxing.BarcodeFormat.PDF_417
import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.BarcodeFormat.RSS_14
import com.google.zxing.BarcodeFormat.RSS_EXPANDED
import com.google.zxing.BarcodeFormat.UPC_A
import com.google.zxing.BarcodeFormat.UPC_E
import com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION
import com.google.zxing.Result
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import javax.inject.Inject
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityScanBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class ScanActivity : ActivityGlobalAbstract(), ZXingScannerView.ResultHandler {
    private lateinit var binding: ActivityScanBinding
    private lateinit var mScannerView: DecoratedBarcodeView
    private lateinit var capture: CaptureManager
    private var isPermissionRequested = false
    private var uid: String? = null
    private var optionSetUid: String? = null
    private var renderingType: ValueTypeRenderingType? = null

    @Inject
    lateinit var scanRepository: ScanRepository

    companion object {
        const val REQUEST_CODE = 101
    }

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
        /*mScannerView.apply {
            setAutoFocus(true)
            when (renderingType) {
                ValueTypeRenderingType.BAR_CODE -> {
                    setFormats(
                        listOf(
                            CODE_39, CODE_128, CODE_93, CODABAR,
                            EAN_13, EAN_8, UPC_A, UPC_E, UPC_EAN_EXTENSION,
                            ITF, PDF_417, RSS_14, RSS_EXPANDED
                        )
                    )
                }
                ValueTypeRenderingType.QR_CODE -> {
                    setFormats(listOf(QR_CODE, DATA_MATRIX, MAXICODE, AZTEC))
                }
                else -> setFormats(ZXingScannerView.ALL_FORMATS)
            }
        }*/
    }

    override fun finish() {
        super.finish()
    }

    override fun onResume() {
        super.onResume()
        /*if (ContextCompat.checkSelfPermission(
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
                REQUEST_CODE
            )
        } else {
            abstractActivity.finish()
        }*/
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
//        mScannerView.stopCamera()
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

    /*private fun initScanner() {
        *//*mScannerView.setResultHandler(this)
        mScannerView.startCamera()*//*
        capture.decode()
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /*when (requestCode) {
            REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    initScanner()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.camera_permission_denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }*/
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun handleResult(result: Result) {
        var url = result.text

        if (optionSetUid != null) {
            val option = scanRepository.getOptions()
                .firstOrNull {
                    it.displayName() == result.text ||
                            it.name() == result.text ||
                            it.code() == result.text
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
