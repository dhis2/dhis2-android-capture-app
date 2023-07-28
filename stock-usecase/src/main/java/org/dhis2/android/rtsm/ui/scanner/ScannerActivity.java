package org.dhis2.android.rtsm.ui.scanner;

import static org.dhis2.android.rtsm.utils.ActivityManager.hasFlash;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.databinding.ActivityScannerBinding;

public class   ScannerActivity extends AppCompatActivity implements
        DecoratedBarcodeView.TorchListener, View.OnClickListener {
    private CaptureManager captureManager;
    private ActivityScannerBinding binding;
    private Boolean flashlightOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force 'portrait' mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);

        binding.flashlightBtnSwitch.setOnClickListener(this);
        flashlightOn = false;

        binding.barcodeScannerBtnClose.setOnClickListener(this);

        // If the device does not have a flashlight in its camera,
        // disable the feature
        if(!hasFlash(getApplicationContext())) {
            binding.flashlightBtnSwitch.setEnabled(false);
        }

        binding.barcodeScanner.setTorchListener(this);

        captureManager = new CaptureManager(this, binding.barcodeScanner);
        captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        captureManager.setShowMissingCameraPermissionDialog(false);
        captureManager.decode();
    }

    @Override
    public void onTorchOn() {
        flashlightOn = true;
        binding.flashlightBtnSwitch.setIconTintResource(R.color.amber);
    }

    @Override
    public void onTorchOff() {
        flashlightOn = false;
        binding.flashlightBtnSwitch.setIconTintResource(R.color.white);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return binding.barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        captureManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.flashlight_btn_switch) {
            toggleFlashlight();
        } else if (id == R.id.barcode_scanner_btn_close) {
            closeScanner();
        }
    }

    private void closeScanner() {
        finish();
    }

    private void toggleFlashlight() {
        if (flashlightOn) {
            binding.barcodeScanner.setTorchOff();
        } else {
            binding.barcodeScanner.setTorchOn();
        }
    }
}