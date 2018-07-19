package com.dhis2.usescases.qrReader;


import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.List;

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
    private boolean isPermissionRequested = false;

    @Inject
    QrReaderContracts.Presenter presenter;
    private String teiUid;
    private List<Trio<String, String, Boolean>> attributes = new ArrayList<>();
    private List<Pair<String, Boolean>> enrollments = new ArrayList<>();
    private List<Pair<String, Boolean>> events = new ArrayList<>();
    private List<Pair<String, Boolean>> relationships = new ArrayList<>();

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
        // TODO CRIS: CHECK THAT ALL JSON BELONG TO SAME TEI
        try {
            QRjson qRjson = new Gson().fromJson(result.getText(), QRjson.class);
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
                case QRjson.EVENTS_JSON:
                    presenter.handleEventInfo(new JSONObject(qRjson.getData()));
                    break;
                case QRjson.RELATIONSHIP_JSON:
                    presenter.handleRelationship(new JSONArray(qRjson.getData()));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Timber.e(e);
            showError(getString(R.string.qr_error_id));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            initScanner();
        } else if (!isPermissionRequested){
            isPermissionRequested = true;
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.CAMERA}, 101);
        }
        else {
            getAbstractActivity().finish();
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
    public void initDownload() {
        binding.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishDownload() {
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    public void goToDashBoard(String uid, boolean isDownloadedOrPresent) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", uid);
        bundle.putString("PROGRAM_UID", null);
        startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }

    @Override
    public void downloadTei(@NonNull String teiUid){
        if (NetworkUtils.isOnline(context)) {
            this.teiUid = teiUid;
            presenter.onlineDownload();
        }
        else {
            renderTeiInfo(teiUid);
        }
    }

    @Override
    public void renderTeiInfo(@Nullable String teiUid) {
        if (teiUid != null) {
            this.teiUid = teiUid;
            promtForMoreQr();
        }
        else {
            showError(getString(R.string.qr_error_id));
        }
    }

    private void showError(String message){
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.QR_SCANNER))
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                    dialog.dismiss();
                    mScannerView.resumeCameraPreview(this);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void renderAttrInfo(@NonNull List<Trio<String, String, Boolean>> attributes) {
        for (Trio<String, String, Boolean> attribute : attributes){
            if (!attribute.val2()){
                showError(getString(R.string.qr_error_attr));
            }
            else if (!this.attributes.contains(attribute)) {
                this.attributes.add(attribute);
            }
        }
        promtForMoreQr();
    }

    @Override
    public void renderEnrollmentInfo(@NonNull  List<Pair<String, Boolean>> enrollments) {
        for (Pair<String, Boolean> enrollment : enrollments){
            if (!enrollment.val1()){
                showError(getString(R.string.qr_error_attr));
            }
            else if (!this.enrollments.contains(enrollment)) {
                this.enrollments.add(enrollment);
            }
        }
        promtForMoreQr();
    }

    @Override
    public void renderEventInfo(@NonNull List<Pair<String, Boolean>> events) {
        for (Pair<String, Boolean> event : events){
            if (!event.val1()){
                showError(getString(R.string.qr_error_attr));
            }
            else if (!this.events.contains(event)) {
                this.events.add(event);
            }
        }
        promtForMoreQr();
    }

    @Override
    public void renderRelationship(@NonNull List<Pair<String, Boolean>> relationships) {
        for (Pair<String, Boolean> relationship : relationships){
            if (!relationship.val1()){
                showError(getString(R.string.qr_error_attr));
            }
            else if (!this.relationships.contains(relationship)) {
                this.relationships.add(relationship);
            }
        }
        promtForMoreQr();
    }

    @Override
    public void promtForMoreQr(){

        // IDENTIFICATION
        String message = getString(R.string.qr_id) + ":\n";
        if (teiUid != null){
            message = message + teiUid + "\n\n";
        }
        else{
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }

        // ATTRIBUTES
        message = message + getString(R.string.qr_attributes) + ":\n";

        if (attributes != null && !attributes.isEmpty()) {
            for (Trio<String, String, Boolean> attribute : attributes) {
                if (attribute.val2()) {
                    message = message + attribute.val1() + "\n";
                }
            }
            message = message + "\n";
        }
        else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }

        // ENROLLMENT
        message = message + getString(R.string.qr_enrollment) + ":\n";

        if (enrollments != null && !enrollments.isEmpty()) {
            for (Pair<String, Boolean> enrollment : enrollments) {
                if (enrollment.val1()) {
                    message = message + enrollment.val0() + "\n";
                }
            }
            message = message + "\n";
        }
        else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }



        // EVENTS
        message = message + getString(R.string.qr_events) + ":\n";

        if (events != null && !events.isEmpty()) {
            int count = 0;
            for (Pair<String, Boolean> event : events) {
                if (event.val1()) {
                    count++;
                }
            }
            message = message + count + " " + getString(R.string.events) + "\n\n";
        }
        else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }



        // RELATIONSHIPS
        message = message + getString(R.string.qr_relationships) + ":\n";

        if (relationships != null && !relationships.isEmpty()) {
            int count = 0;
            for (Pair<String, Boolean> relationship : relationships) {
                if (relationship.val1()) {
                    count++;
                }
            }
            message = message + count + " " + getString(R.string.relationships) + "\n";
        }
        else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }


        // READ MORE
        message = message + "\n\n" + getString(R.string.read_more_qr);

        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.QR_SCANNER))
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                    dialog.dismiss();
                    mScannerView.resumeCameraPreview(this);
                })
                .setNegativeButton(getString(R.string.save_qr), (dialog, which) -> {
                    presenter.download();
                    dialog.dismiss();
                })
                .show();
    }
}
