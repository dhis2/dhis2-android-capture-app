package org.dhis2.utils.custom_views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormCoordinatesAccentBinding;
import org.dhis2.databinding.FormCoordinatesBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST;

/**
 * QUADRAM. Created by Administrador on 21/03/2018.
 */

public class CoordinatesView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private ViewDataBinding binding;
    private TextInputEditText latitude;
    private TextInputEditText longitude;
    private TextInputLayout latitudeInputLayout;
    private TextInputLayout longitudeInputLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private OnMapPositionClick listener;
    private OnCurrentLocationClick listener2;
    private FlowableProcessor<RowAction> processor;
    private String uid;
    private TextView errorView;
    private View clearButton;

    public CoordinatesView(Context context) {
        super(context);
        init(context);
    }

    public CoordinatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CoordinatesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        super.init(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates_accent, this, true);

        latitudeInputLayout = findViewById(R.id.latitudeInputLayout);
        longitudeInputLayout = findViewById(R.id.longInputLayout);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);

        errorView = findViewById(R.id.errorMessage);
        clearButton = findViewById(R.id.clearButton);

        latitude.setOnEditorActionListener((v, actionId, event) -> {
            if (validateCoordinates()) {
                Double latitudeValue = isEmpty(latitude.getText().toString()) ? null : Double.valueOf(latitude.getText().toString());
                Double longitudeValue = isEmpty(longitude.getText().toString()) ? null : Double.valueOf(longitude.getText().toString());
                listener2.onCurrentLocationClick(latitudeValue, longitudeValue);
            } else {
                longitude.requestFocus();
                longitude.performClick();
            }
            return true;
        });

        longitude.setOnEditorActionListener((v, actionId, event) -> {
            if (validateCoordinates()) {
                Double latitudeValue = isEmpty(latitude.getText().toString()) ? null : Double.valueOf(latitude.getText().toString());
                Double longitudeValue = isEmpty(longitude.getText().toString()) ? null : Double.valueOf(longitude.getText().toString());
                listener2.onCurrentLocationClick(latitudeValue, longitudeValue);
            } else {
                latitude.requestFocus();
                latitude.performClick();
            }
            return true;

        });

        latitude.setFocusable(true); //Makes editText editable
        latitude.setClickable(true);//  and clickable

        longitude.setFocusable(true); //Makes editText editable
        longitude.setClickable(true);//  and clickable

        ImageButton position = findViewById(R.id.location1);
        ImageButton map = findViewById(R.id.location2);

        position.setOnClickListener(this);
        map.setOnClickListener(this);
        clearButton.setOnClickListener(this);
    }

    private boolean validateCoordinates() {

        return (!isEmpty(latitude.getText()) && !isEmpty(longitude.getText())) ||
                (isEmpty(latitude.getText()) && isEmpty(longitude.getText()));
    }

    public void setMapListener(OnMapPositionClick listener) {
        this.listener = listener;
    }

    public void setCurrentLocationListener(OnCurrentLocationClick listener) {
        this.listener2 = listener;
    }

    public void setLabel(String label) {
        this.label = label;
        if (binding instanceof FormCoordinatesBinding)
            ((FormCoordinatesBinding) binding).setLabel(label);
        else
            ((FormCoordinatesAccentBinding) binding).setLabel(label);
    }

    public void setDescription(String description) {
        if (binding instanceof FormCoordinatesBinding)
            ((FormCoordinatesBinding) binding).setDescription(description);
        else
            ((FormCoordinatesAccentBinding) binding).setDescription(description);
    }

    public void setInitialValue(String initialValue) {
        String[] latLongValue = initialValue.replace("[", "").replace("]", "").replace(" ", "").split(",");
        this.latitude.setText(String.format(Locale.getDefault(), "%.5f", Double.valueOf(latLongValue[1])));
        this.longitude.setText(String.format(Locale.getDefault(), "%.5f", Double.valueOf(latLongValue[0])));
        this.clearButton.setVisibility(VISIBLE);

    }

    public void setWarning(String msg) {
        if (!isEmpty(msg)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_color));
            errorView.setText(msg);
            errorView.setVisibility(VISIBLE);
        } else
            errorView.setVisibility(GONE);
    }

    public void setError(String msg) {
        if (!isEmpty(msg)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_color));
            errorView.setText(msg);
            errorView.setVisibility(VISIBLE);
            clearValueData();
        } else
            errorView.setVisibility(GONE);
    }

    @Override
    public void onClick(View view) {
        activate();
        switch (view.getId()) {
            case R.id.location1:
                getLocation();
                break;
            case R.id.location2:
                if (listener != null)
                    listener.onMapPositionClick(this);
                else
                    ((OnMapPositionClick) getContext()).onMapPositionClick(this);
                break;
            case R.id.clearButton:
                clearValueData();
                updateLocation(null, null);
                break;
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((ActivityGlobalAbstract) getContext(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
        } else {

            mFusedLocationClient.getLastLocation().
                    addOnSuccessListener(location -> {
                        if (location != null)
                            updateLocation(location.getLatitude(), location.getLongitude());
                        else
                            startRequestingLocation();
                    });
        }
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setEditable(Boolean editable) {
        latitude.setEnabled(editable);
        longitude.setEnabled(editable);
        findViewById(R.id.location1).setEnabled(editable);
        findViewById(R.id.location2).setEnabled(editable);
    }

    public void setProcessor(String uid, FlowableProcessor<RowAction> processor) {
        this.processor = processor;
        this.uid = uid;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            activate();
            latitude.performClick();
        }
    }


    public interface OnMapPositionClick {
        void onMapPositionClick(CoordinatesView coordinatesView);
    }

    public interface OnCurrentLocationClick {
        void onCurrentLocationClick(Double latitude, Double longitude);
    }

    @SuppressLint("MissingPermission")
    public void updateLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            String lat = String.format(Locale.getDefault(), "%.5f", latitude);
            String lon = String.format(Locale.getDefault(), "%.5f", longitude);
            this.latitude.setText(lat);
            this.longitude.setText(lon);
            this.clearButton.setVisibility(VISIBLE);
            if (uid != null) {
                processor.onNext(
                        RowAction.create(uid,
                                String.format(Locale.US,
                                        "[%.5f,%.5f]", longitude, latitude))
                );
                nextFocus(this);
            }
        }
        listener2.onCurrentLocationClick(latitude, longitude);
        invalidate();
    }

    private void startRequestingLocation() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Double latitude = locationResult.getLocations().get(0).getLatitude();
                    Double longitude = locationResult.getLocations().get(0).getLongitude();
                    updateLocation(latitude, longitude);
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((ActivityGlobalAbstract) getContext(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
        } else
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    public void clearValueData() {

        this.latitude.setText(null);
        this.longitude.setText(null);
        this.clearButton.setVisibility(GONE);

    }
}

