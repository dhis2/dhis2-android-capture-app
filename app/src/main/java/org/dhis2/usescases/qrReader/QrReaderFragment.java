package org.dhis2.usescases.qrReader;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.qr.QRjson;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentQrBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


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
    private String eventUid;
    private List<Trio<TrackedEntityDataValue, String, Boolean>> eventData = new ArrayList<>();
    private List<Trio<TrackedEntityDataValue, String, Boolean>> teiEventData = new ArrayList<>();

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

        try {
            QRjson qRjson = new Gson().fromJson(result.getText(), QRjson.class);
            switch (qRjson.getType()) {
                case QRjson.EVENT_JSON:
                    presenter.handleEventWORegistrationInfo(new JSONObject(qRjson.getData()));
                    break;
                case QRjson.DATA_JSON:
                    presenter.handleDataInfo(new JSONArray(qRjson.getData()));
                    break;
                case QRjson.DATA_JSON_WO_REGISTRATION:
                    presenter.handleDataWORegistrationInfo(new JSONArray(qRjson.getData()));
                    break;
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
        } else if (!isPermissionRequested) {
            isPermissionRequested = true;
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
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
    public void goToDashBoard(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", uid);
        bundle.putString("PROGRAM_UID", null);
        startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }

    @Override
    public void goToEvent(String eventUid, String programId, String orgUnit) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(ORG_UNIT, orgUnit);
        startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void showIdError() {
        showError(getString(R.string.qr_no_id_error));
    }

    @Override
    public void downloadEventWORegistration(@NonNull String eventUid) {
        this.eventUid = eventUid;
        renderEventWORegistrationInfo(eventUid);
    }

    @Override
    public void renderEventWORegistrationInfo(@Nullable String eventUid) {
        if (eventUid != null) {
            this.eventUid = eventUid;
            promtForEventWORegistrationMoreQr();
        } else {
            showError(getString(R.string.qr_error_id));
        }
    }

    @Override
    public void downloadTei(@NonNull String teiUid) {
        if (NetworkUtils.isOnline(context)) {
            this.teiUid = teiUid;
            presenter.onlineDownload();
        } else {
            renderTeiInfo(teiUid);
        }
    }

    @Override
    public void renderTeiInfo(@Nullable String teiUid) {
        if (teiUid != null) {
            this.teiUid = teiUid;
            promtForTEIMoreQr();
        } else {
            showError(getString(R.string.qr_error_id));
        }
    }

    private void showError(String message) {
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
    public void renderEventDataInfo(@NonNull List<Trio<TrackedEntityDataValue, String, Boolean>> data) {
        for (Trio<TrackedEntityDataValue, String, Boolean> dataValue : data) {
            if (!dataValue.val2()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.eventData.contains(dataValue)) {
                this.eventData.add(dataValue);
            }
        }
        promtForEventWORegistrationMoreQr();
    }

    @Override
    public void renderTeiEventDataInfo(@NonNull List<Trio<TrackedEntityDataValue, String, Boolean>> data) {
        for (Trio<TrackedEntityDataValue, String, Boolean> dataValue : data) {
            if (!dataValue.val2()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.teiEventData.contains(dataValue)) {
                this.teiEventData.add(dataValue);
            }
        }
        promtForTEIMoreQr();
    }

    @Override
    public void renderAttrInfo(@NonNull List<Trio<String, String, Boolean>> attributes) {
        for (Trio<String, String, Boolean> attribute : attributes) {
            if (!attribute.val2()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.attributes.contains(attribute)) {
                this.attributes.add(attribute);
            }
        }
        promtForTEIMoreQr();
    }

    @Override
    public void renderEnrollmentInfo(@NonNull List<Pair<String, Boolean>> enrollments) {
        for (Pair<String, Boolean> enrollment : enrollments) {
            if (!enrollment.val1()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.enrollments.contains(enrollment)) {
                this.enrollments.add(enrollment);
            }
        }
        promtForTEIMoreQr();
    }

    @Override
    public void renderEventInfo(@NonNull List<Pair<String, Boolean>> events) {
        for (Pair<String, Boolean> event : events) {
            if (!event.val1()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.events.contains(event)) {
                this.events.add(event);
            }
        }
        promtForTEIMoreQr();
    }

    @Override
    public void renderRelationship(@NonNull List<Pair<String, Boolean>> relationships) {
        for (Pair<String, Boolean> relationship : relationships) {
            if (!relationship.val1()) {
                showError(getString(R.string.qr_error_attr));
            } else if (!this.relationships.contains(relationship)) {
                this.relationships.add(relationship);
            }
        }
        promtForTEIMoreQr();
    }

    @Override
    public void promtForTEIMoreQr() {

        // IDENTIFICATION
        String message = getString(R.string.qr_id) + ":\n";
        if (teiUid != null) {
            message = message + teiUid + "\n\n";
        } else {
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
        } else {
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
        } else {
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
        } else {
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
        } else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }

        // ATTRIBUTES
        message = message + getString(R.string.qr_data_values) + ":\n";

        if (teiEventData != null && !teiEventData.isEmpty()) {
            for (Trio<TrackedEntityDataValue, String, Boolean> attribute : teiEventData) {
                message = message + attribute.val1() + ":\n" + attribute.val0().value() + "\n\n";
            }
            message = message + "\n";
        } else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }

        // READ MORE
        message = message + "\n\n" + getString(R.string.read_more_qr);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.QR_SCANNER))
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                    dialog.dismiss();
                    mScannerView.resumeCameraPreview(this);
                })
                .setNegativeButton(getString(R.string.save_qr), (dialog, which) -> {
                    presenter.download();
                    dialog.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        });
        alertDialog.show();
    }

    @Override
    public void promtForEventWORegistrationMoreQr() {

        // IDENTIFICATION
        String message = getString(R.string.qr_id) + ":\n";
        if (eventUid != null) {
            message = message + eventUid + "\n\n";
        } else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }

        // ATTRIBUTES
        message = message + getString(R.string.qr_data_values) + ":\n";

        if (eventData != null && !eventData.isEmpty()) {
            for (Trio<TrackedEntityDataValue, String, Boolean> attribute : eventData) {
                message = message + attribute.val1() + ":\n" + attribute.val0().value() + "\n\n";
            }
            message = message + "\n";
        } else {
            message = message + getString(R.string.qr_no_data) + "\n\n";
        }


        // READ MORE
        message = message + "\n\n" + getString(R.string.read_more_qr);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.QR_SCANNER))
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                    dialog.dismiss();
                    mScannerView.resumeCameraPreview(this);
                })
                .setNegativeButton(getString(R.string.save_qr), (dialog, which) -> {
                    presenter.downloadEventWORegistration();
                    dialog.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        });
        alertDialog.show();
    }
}
