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

import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


/**
 * Created by Cristian on 15/03/2018.
 */

public class MapSelectorActivity extends ActivityGlobalAbstract implements MapActivityLocationCallback.OnLocationChanged {

    private MapView mapView;
    private MapboxMap map;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 102;
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private TextView latLon;
    private boolean init = false;
    private Style style;

    @NonNull
    public static Intent create(@NonNull Activity activity) {
        return new Intent(activity, MapSelectorActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_map_selector);
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
                this.style = style;
                enableLocationComponent();
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
    @SuppressWarnings("MissingPermission")
    private void enableLocationComponent() {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = map.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            locationComponent.zoomWhileTracking(13.0);

            LocationEngineProvider.getBestLocationEngine(this).getLastLocation(new MapActivityLocationCallback(this));

        } else {

            /*  permissionsManager = PermissionsManager(this)
              permissionsManager?.requestLocationPermissions(this)*/
        }
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

    @Override
    public void onLocationChanged(@NotNull LatLng latLng) {
        Timber.d("NEW LOCATION %s, %s", latLng.getLatitude(), latLng.getLongitude());

        if (!init) {
            init = true;
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.getLatitude(), latLng.getLongitude()), 13.0));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latLng.getLatitude(), latLng.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15.0)               // Sets the zoom
                    .build();                // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}