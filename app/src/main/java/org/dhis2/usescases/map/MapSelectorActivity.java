package org.dhis2.usescases.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.jetbrains.annotations.NotNull;


/**
 * Created by Cristian on 15/03/2018.
 */

public class MapSelectorActivity extends ActivityGlobalAbstract {

    private MapView mapView;
    private MapboxMap map;
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
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_map_selector);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.fab).setOnClickListener(v -> {
            if (map != null && map.getCameraPosition().target != null) {
                Intent data = new Intent();
                data.putExtra(LATITUDE, String.valueOf(map.getCameraPosition().target.getLatitude()));
                data.putExtra(LONGITUDE, String.valueOf(map.getCameraPosition().target.getLongitude()));
                setResult(RESULT_OK, data);
                finish();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        latLon = findViewById(R.id.latlon);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                centerMapOnCurrentLocation();
            });
            map.addOnCameraIdleListener(() -> {
                if (map.getCameraPosition().target != null) {
                    String latLonText = map.getCameraPosition().target.getLatitude() + " : " + map.getCameraPosition().target.getLongitude();
                    latLon.setText(latLonText);
                }
            });
        });
    }

    // Add the mapView's own lifecycle methods to the activity's lifecycle methods
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void centerMapOnCurrentLocation() {
        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // TODO CRIS
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }*/
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(15)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION_PERMISSION_REQUEST: {
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