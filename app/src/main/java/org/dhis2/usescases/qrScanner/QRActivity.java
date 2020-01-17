package org.dhis2.usescases.qrScanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.dhis2.R;
import org.dhis2.databinding.ActivityQrBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * QUADRAM. Created by ppajuelo on 15/01/2018.
 */

public class QRActivity extends ActivityGlobalAbstract implements ZXingScannerView.ResultHandler {

    ActivityQrBinding binding;
    private ZXingScannerView mScannerView;
    private boolean isPermissionRequested = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr);
        mScannerView = binding.scannerView;
        mScannerView.setAutoFocus(true);
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.CODE_128);
        mScannerView.setFormats(formats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            initScanner();
        } else if (!isPermissionRequested) {
            isPermissionRequested = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            getAbstractActivity().finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    private void initScanner() {
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initScanner();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void handleResult(Result result) {
        String url = result.getText();
        Intent data = new Intent();
        data.putExtra(Constants.EXTRA_DATA, url);
        setResult(RESULT_OK, data);
        finish();
    }
}
