package com.dhis2.usescases.qrReader;


import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.qr.QRjson;
import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;
import com.dhis2.databinding.FragmentQrBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.main.MainActivity;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * A simple {@link Fragment} subclass.
 */
public class QrReaderFragment extends FragmentGlobalAbstract implements ZXingScannerView.ResultHandler, QrReaderContracts.View {

    private ZXingScannerView mScannerView;
    private Context context;
    FragmentQrBinding binding;

    @Inject
    QrReaderContracts.Presenter presenter;

    public QrReaderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new QrReaderModule()).inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr, container, false);
        presenter.init(this);
        mScannerView = binding.scannerView;
        mScannerView.setAutoFocus(true);
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
        return binding.getRoot();
    }

    @Override
    public void handleResult(Result result) {
        QRjson qRjson = new Gson().fromJson(result.getText(), QRjson.class);

        try {
            switch (qRjson.getType()) {
                case QRjson.TEI_JSON:
                    presenter.handleTeiInfo(new JSONObject(qRjson.getData()));
                    break;
                case QRjson.ATTR_JSON:
                    presenter.handleAttrInfo(new JSONArray(qRjson.getData()));
                    break;
                case QRjson.ENROLLMENT_JSON:
                    presenter.handleEnrollmentInfo(new JSONArray(qRjson.getData()));
                    break;
            }
        } catch (Exception e) {

        }
        mScannerView.resumeCameraPreview(this);

    }


    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            initScanner();
        } else {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults[0] == PERMISSION_GRANTED)
            initScanner();
    }

    private void initScanner() {
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }


    @Override
    public void renderTeiInfo(String info, boolean isOk) {
        binding.info.setText(info);
        if (isOk) {
            //TODO: Show download message if network connection is avaible
        } else {
            //TODO: Show warning
        }

    }

    @Override
    public void renderAttrInfo(ArrayList<Trio<String, String, Boolean>> attributes) {
        binding.info.append("\nAttributes info");
        for (Trio trio : attributes) {
            binding.info.append(String.format("\n%s: %s %s", trio.val0(), trio.val1(), (boolean) trio.val2() ? "" : "Can't be saved"));
        }
    }

    @Override
    public void renderEnrollmentInfo(ArrayList<Pair<String, Boolean>> enrollment) {
        binding.info.append("\nEnrollment info");
        for (Pair pair : enrollment) {
            binding.info.append(String.format("\nEnrollment for %s %s", pair.val0(), (boolean) pair.val1() ? "" : "Can't be saved"));
        }
    }

    @Override
    public void initDownload() {
        //TODO: Change fab to progress
        displayMessage("Saving... please wait");
    }

    @Override
    public void finishDownload() {
        //TODO: Prompt user.
        displayMessage("Done");
    }
}
