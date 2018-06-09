package com.dhis2.usescases.qrReader;


import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.utils.NetworkUtils;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import timber.log.Timber;

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
            Timber.e(e);
        }
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

        if (isOk && NetworkUtils.isOnline(context)) {
            presenter.onlineDownload();
        } else {
            new AlertDialog.Builder(context, R.style.CustomDialog)
                    .setTitle(getString(R.string.QR_SCANNER))
                    .setMessage(info)
                    .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                        dialog.dismiss();
                        mScannerView.resumeCameraPreview(this);
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void renderAttrInfo(ArrayList<Trio<String, String, Boolean>> attributes) {

    }

    @Override
    public void renderEnrollmentInfo(ArrayList<Pair<String, Boolean>> enrollment) {

    }

    @Override
    public void initDownload() {
        binding.progress.setVisibility(View.VISIBLE);
    }


    @Override
    public void goToDashBoard(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", uid);
        bundle.putString("PROGRAM_UID", null);
        startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }
}
