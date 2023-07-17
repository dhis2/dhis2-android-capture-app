package org.dhis2.form.data.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.google.zxing.client.android.Intents
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ScanOptions

class ScanCaptureActivity : Activity() {
    private lateinit var barcodeScanner: DecoratedBarcodeView
    private lateinit var capture: ScanCaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zxing_capture)
        barcodeScanner = findViewById(R.id.zxing_barcode_scanner)
        capture = ScanCaptureManager(this, barcodeScanner).apply {
            initializeFromIntent(intent, savedInstanceState)
            decode()
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
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}

fun Context.scanCaptureActivityIntent(scanOptions: ScanOptions?): Intent {
    val intent = Intent(this, ScanCaptureActivity::class.java)
    intent.action = Intents.Scan.ACTION
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    scanOptions?.let {
        it.moreExtras.entries.forEach {
            val key = it.key
            when (val value = it.value) {
                is Int -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is Double -> intent.putExtra(key, value)
                is Float -> intent.putExtra(key, value)
                is Bundle -> intent.putExtra(key, value)
                is IntArray -> intent.putExtra(key, value)
                is LongArray -> intent.putExtra(key, value)
                is BooleanArray -> intent.putExtra(key, value)
                is DoubleArray -> intent.putExtra(key, value)
                is FloatArray -> intent.putExtra(key, value)
                is CharArray -> intent.putExtra(key, value)
                else -> intent.putExtra(key, value.toString())
            }
        }
    }
    return intent
}
