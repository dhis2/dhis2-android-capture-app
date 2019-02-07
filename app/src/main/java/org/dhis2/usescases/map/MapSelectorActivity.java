package org.dhis2.usescases.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by Cristian on 15/03/2018.
 *
 */

public class MapSelectorActivity extends ActivityGlobalAbstract implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 102;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private TextView latLon;

    @NonNull
    public static Intent create(@NonNull Activity activity) {
        return new Intent(activity, MapSelectorActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selector);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.fab).setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(LATITUDE, String.valueOf(mMap.getCameraPosition().target.latitude));
            data.putExtra(LONGITUDE, String.valueOf(mMap.getCameraPosition().target.longitude));
            setResult(RESULT_OK, data);
            finish();
        });

        latLon = findViewById(R.id.latlon);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(() -> {
            String latLonText = mMap.getCameraPosition().target.latitude + " : " + mMap.getCameraPosition().target.longitude;
            latLon.setText(latLonText);
        });
        centerMapOnCurrentLocation();
    }

    private void centerMapOnCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // TODO CRIS
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
            }
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case EventInitialPresenter.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centerMapOnCurrentLocation();
                } else {
                    // TODO CRIS
                }
            }
        }
    }
}
