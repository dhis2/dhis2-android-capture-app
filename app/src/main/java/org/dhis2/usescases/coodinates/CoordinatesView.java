package org.dhis2.usescases.coodinates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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

import org.dhis2.Bindings.DoubleExtensionsKt;
import org.dhis2.Bindings.StringExtensionsKt;
import org.dhis2.R;
import org.dhis2.databinding.FormCoordinatesAccentBinding;
import org.dhis2.databinding.FormCoordinatesBinding;
import org.dhis2.uicomponents.map.geometry.LngLatValidatorKt;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.customviews.FieldLayout;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.maintenance.D2Error;

import java.util.List;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;

public class CoordinatesView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private ViewDataBinding binding;
    private TextInputEditText latitude;
    private TextInputEditText longitude;
    private TextInputLayout latitudeInputLayout;
    private TextInputLayout longitudeInputLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private ImageButton location1;
    private OnMapPositionClick mapLocationListener;
    private OnCurrentLocationClick currentLocationListener;
    private TextView errorView;
    private View clearButton;
    private View polygonInputLayout;
    private TextInputEditText polygon;
    private FeatureType featureType;
    private Geometry currentGeometry;

    public CoordinatesView(Context context) {
        super(context);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public CoordinatesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public CoordinatesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            init(context);
        else
            initEditor();
    }

    public void initEditor() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_coordinates, this, true);
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

        polygonInputLayout = findViewById(R.id.polygonInputLayuout);
        polygon = findViewById(R.id.polygonEditText);
        latitudeInputLayout = findViewById(R.id.latitudeInputLayout);
        longitudeInputLayout = findViewById(R.id.longInputLayout);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        location1 = findViewById(R.id.location1);

        errorView = findViewById(R.id.errorMessage);
        clearButton = findViewById(R.id.clearButton);

        latitude.setOnEditorActionListener((v, actionId, event) -> {
            if (validateCoordinates()) {
                Double latitudeValue = isEmpty(latitude.getText().toString()) ? null : DoubleExtensionsKt.truncate(getLatitude());
                Double longitudeValue = isEmpty(longitude.getText().toString()) ? null : DoubleExtensionsKt.truncate(getLongitude());
                if (latitudeValue != null || longitudeValue != null) {
                    if (LngLatValidatorKt.isLatitudeValid(latitudeValue)) {
                        currentLocationListener.onCurrentLocationClick(GeometryHelper.createPointGeometry(longitudeValue, latitudeValue));
                    } else {
                        setError(getContext().getString(R.string.coordinates_error));
                    }
                }
            } else {
                longitude.requestFocus();
                longitude.performClick();
            }
            return true;
        });

        longitude.setOnEditorActionListener((v, actionId, event) -> {
            if (validateCoordinates()) {
                Double latitudeValue = isEmpty(latitude.getText().toString()) ? null : DoubleExtensionsKt.truncate(getLatitude());
                Double longitudeValue = isEmpty(longitude.getText().toString()) ? null : DoubleExtensionsKt.truncate(getLongitude());
                if (latitudeValue != null || longitudeValue != null) {
                    if (LngLatValidatorKt.areLngLatCorrect(longitudeValue, latitudeValue)) {
                        currentLocationListener.onCurrentLocationClick(GeometryHelper.createPointGeometry(longitudeValue, latitudeValue));
                    } else {
                        setError(getContext().getString(R.string.coordinates_error));
                    }
                }
            } else {
                latitude.requestFocus();
                latitude.performClick();
            }
            return true;

        });

        polygon.setFocusable(false);
        polygon.setClickable(false);

        latitude.setFocusable(true);
        latitude.setClickable(true);

        longitude.setFocusable(true);
        longitude.setClickable(true);

        ImageButton position = findViewById(R.id.location1);
        ImageButton map = findViewById(R.id.location2);

        position.setOnClickListener(this);
        map.setOnClickListener(this);
        clearButton.setOnClickListener(this);

        longitude.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                if (!longitude.getText().toString().isEmpty()) {
                    Double lon = DoubleExtensionsKt.truncate(getLongitude());
                    longitude.setText(lon.toString());
                    if (!latitude.getText().toString().isEmpty()) {
                        Double lat = getLatitude();
                        if (!LngLatValidatorKt.areLngLatCorrect(lon, lat)) {
                            setError(getContext().getString(R.string.coordinates_error));
                        } else {
                            setError(null);
                        }
                    }
                }
            }
        });

        latitude.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
                if (!latitude.getText().toString().isEmpty()) {
                    Double lat = DoubleExtensionsKt.truncate(getLatitude());
                    latitude.setText(lat.toString());
                    if (!longitude.getText().toString().isEmpty()) {
                        Double lon = getLongitude();
                        if (!LngLatValidatorKt.areLngLatCorrect(lon, lat)) {
                            setError(getContext().getString(R.string.coordinates_error));
                        } else {
                            setError(null);
                        }
                    }
                }
        });
    }

    private boolean validateCoordinates() {

        return (!isEmpty(latitude.getText()) && !isEmpty(longitude.getText())) ||
                (isEmpty(latitude.getText()) && isEmpty(longitude.getText()));
    }

    public void setMapListener(OnMapPositionClick mapLocationListener) {
        this.mapLocationListener = mapLocationListener;
    }

    public void setCurrentLocationListener(OnCurrentLocationClick currentLocationListener) {
        this.currentLocationListener = currentLocationListener;
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

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
        latitudeInputLayout.setVisibility(featureType == FeatureType.POINT ? View.VISIBLE : View.GONE);
        longitudeInputLayout.setVisibility(featureType == FeatureType.POINT ? View.VISIBLE : View.GONE);
        polygonInputLayout.setVisibility(featureType != FeatureType.POINT ? View.VISIBLE : View.GONE);
        location1.setVisibility(featureType == FeatureType.POINT ? View.VISIBLE : View.GONE);
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void setInitialValue(String initialValue) {
        if (featureType == null)
            throw new NullPointerException("use setFeatureType before setting an initial value");
        setCoordinatesValue(
                Geometry.builder()
                        .coordinates(initialValue)
                        .type(featureType)
                        .build());
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
                if (mapLocationListener != null)
                    mapLocationListener.onMapPositionClick(this);
                else
                    ((OnMapPositionClick) getContext()).onMapPositionClick(this);
                break;
            case R.id.clearButton:
                clearValueData();
                updateLocation(null);
                if (errorView.getVisibility()== VISIBLE) {
                    setError(null);
                }
                break;
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(this.getContext() instanceof ActivityGlobalAbstract){
                ((ActivityGlobalAbstract)this.getContext()).requestLocationPermission(this);
            }
        } else {

            mFusedLocationClient.getLastLocation().
                    addOnSuccessListener(location -> {
                        if (location != null) {
                            double longitude = DoubleExtensionsKt.truncate(location.getLongitude());
                            double latitude = DoubleExtensionsKt.truncate(location.getLatitude());
                            updateLocation(GeometryHelper.createPointGeometry(longitude, latitude));
                        } else {
                            startRequestingLocation();
                        }
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
        clearButton.setEnabled(editable);
        clearButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        findViewById(R.id.location1).setEnabled(editable);
        findViewById(R.id.location2).setEnabled(editable);
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
        void onCurrentLocationClick(Geometry geometry);
    }

    @SuppressLint("MissingPermission")
    public void updateLocation(Geometry geometry) {

        setCoordinatesValue(geometry);

        this.currentGeometry = geometry;
        if (currentLocationListener != null)
            currentLocationListener.onCurrentLocationClick(geometry);
        invalidate();
    }

    private void setCoordinatesValue(Geometry geometry) {
        if (geometry != null && geometry.type() != null) {
            if (geometry.type() == FeatureType.POINT) {
                try {
                    List<Double> list = GeometryHelper.getPoint(geometry);
                    this.latitude.setText(String.valueOf(list.get(1)));
                    this.longitude.setText(String.valueOf(list.get(0)));
                } catch (D2Error d2Error) {
                    d2Error.printStackTrace();
                }

            } else if (geometry.type() == FeatureType.POLYGON) {
                this.polygon.setText(getContext().getString(R.string.polygon_captured));
            } else if (geometry.type() == FeatureType.MULTI_POLYGON) {
                this.polygon.setText(getContext().getString(R.string.polygon_captured));
            }
            this.clearButton.setVisibility(VISIBLE);
        }
    }

    private void startRequestingLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Double latitude = DoubleExtensionsKt.truncate(locationResult.getLocations().get(0).getLatitude());
                    Double longitude = DoubleExtensionsKt.truncate(locationResult.getLocations().get(0).getLongitude());
                    updateLocation(GeometryHelper.createPointGeometry(longitude, latitude));
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((ActivityGlobalAbstract) getContext(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_LOCATION_PERMISSION_REQUEST);
        } else
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    public void clearValueData() {
        this.latitude.setText(null);
        this.longitude.setText(null);
    }

    public Double getLatitude() {
        String latString = StringExtensionsKt.parseToDouble(latitude.getText().toString());
        return Double.valueOf(latString);
    }

    public Double getLongitude() {
        String lonString = StringExtensionsKt.parseToDouble(longitude.getText().toString());
        return Double.valueOf(lonString);
    }

    public String currentCoordinates() {
        return currentGeometry != null ? currentGeometry.coordinates() : null;
    }
}

