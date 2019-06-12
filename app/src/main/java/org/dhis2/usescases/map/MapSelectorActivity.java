package org.dhis2.usescases.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.jetbrains.annotations.NotNull;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;


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
    private Style style;
    private FloatingActionButton save;
    private static List<List<Point>> points = new ArrayList<>();
    private static int index = 0;
    private static final List<Point> OUTER_POINTS = new ArrayList<>();
    private Source source;

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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (points.size() <= index) {
                    points.add(new ArrayList<>());
                }
                Point point = Point.fromLngLat(map.getCameraPosition().target.getLongitude(),
                        map.getCameraPosition().target.getLatitude());
                points.get(index).add( point);
                addPoint(point);
                updateVector();

            }
        });
        save = findViewById(R.id.save);
        save.setOnClickListener(v -> {
            updateVector();
           /*
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
            */
        });
        FloatingActionButton add = findViewById(R.id.add);
        add.setOnClickListener(v -> {
            index++;
        });
        latLon = findViewById(R.id.latlon);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                this.style = style;
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

    public void addPoint(Point point) {
        String uuid = UUID.randomUUID().toString();
        style.addImage(uuid,
                BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource(uuid, Feature.fromGeometry(
                point));
        style.addSource(geoJsonSource);

        SymbolLayer symbolLayer = new SymbolLayer(uuid, uuid);
        symbolLayer.withProperties(
                PropertyFactory.iconImage(uuid)
        );
        style.addLayer(symbolLayer);
    }

    public void updateVector() {
        List<List<Point>> list = new ArrayList<>();
        for (List<Point> pointsList: points) {
            if (pointsList.size() == 1) {
                pointsList.add(pointsList.get(0));
            }
            if (pointsList.get(0).longitude() != pointsList.get(pointsList.size() - 1).longitude()
            && pointsList.get(0).latitude() != pointsList.get(pointsList.size() - 1).latitude()) {
                pointsList.add(pointsList.get(0));
            }
            list.add(pointsList);
        }
        if (style.getSource("source") == null) {
            style.addSource(new GeoJsonSource("source", Polygon.fromLngLats(list)));
            style.addLayer(new FillLayer("layer", "source").withProperties(
                    fillColor(Color.parseColor("#3bb2d0")))
            );
        } else {
            ((GeoJsonSource) style.getSource("source")).setGeoJson(Polygon.fromLngLats(list));
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