package org.dhis2.usescases.qrScanner;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import org.dhis2.R;
import org.dhis2.databinding.ActivityQrBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by ppajuelo on 15/01/2018.
 */

public class QRActivity extends ActivityGlobalAbstract {

    ActivityQrBinding binding;
    CameraSource cameraSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true)
                .build();
        binding.cameraView.getHolder().addCallback(mySurfaceCallback);

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    String qrCode = barcodes.valueAt(0).rawValue;
                    Intent data = new Intent();
                    data.putExtra(Constants.EXTRA_DATA, qrCode);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.stop();
    }

    private SurfaceHolder.Callback mySurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setUpSourceCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private void setUpSourceCamera() {
        try {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PERMISSION_GRANTED && cameraSource != null)
                cameraSource.start(binding.cameraView.getHolder());
            else
                ActivityCompat.requestPermissions(QRActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
        } catch (IOException e) {
            Log.d("CAMERA SOURCE", e.getMessage());
        }
    }

}
