package com.dhis2.utils.CustomViews;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhis2.R;
import com.dhis2.databinding.FormCoordinatesAccentBinding;
import com.dhis2.databinding.FormCoordinatesBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST;

/**
 * Created by Administrador on 21/03/2018.
 */

public class CoordinatesView extends RelativeLayout implements View.OnClickListener {

    private ViewDataBinding binding;
    private TextView latLong;
    private FusedLocationProviderClient mFusedLocationClient;
    private OnMapPositionClick listener;
    private OnCurrentLocationClick listener2;
    private boolean isBgTransparent;
    private LayoutInflater inflater;


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

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }


    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates_accent, this, true);

        latLong = findViewById(R.id.latlong);

        ImageButton position = findViewById(R.id.location1);
        ImageButton map = findViewById(R.id.location2);

        position.setOnClickListener(this);
        map.setOnClickListener(this);
    }

    public void setMapListener(OnMapPositionClick listener) {
        this.listener = listener;
    }

    public void setCurrentLocationListener(OnCurrentLocationClick listener) {
        this.listener2 = listener;
    }

    public void setLabel(String label) {
        if (binding instanceof FormCoordinatesBinding)
            ((FormCoordinatesBinding) binding).setLabel(label);
        else
            ((FormCoordinatesAccentBinding) binding).setLabel(label);
    }

    public void setInitialValue(String initialValue) {
        this.latLong.setText(initialValue.replace("[", "").replace("]", ""));
    }

    @Override
    public void onClick(View view) {
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
                    });
        }
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }


    public interface OnMapPositionClick {
        void onMapPositionClick(CoordinatesView coordinatesView);
    }

    public interface OnCurrentLocationClick {
        void onCurrentLocationClick(double latitude, double longitude);
    }

    public void updateLocation(double latitude, double longitude) {
        String lat = String.format(Locale.getDefault(), "%.5f", latitude);
        String lon = String.format(Locale.getDefault(), "%.5f", longitude);
        this.latLong.setText(String.format("%s, %s", lat, lon));
        listener2.onCurrentLocationClick(latitude, longitude);
        invalidate();
    }
}

