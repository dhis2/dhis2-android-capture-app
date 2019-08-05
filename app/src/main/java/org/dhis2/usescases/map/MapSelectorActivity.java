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
import com.google.gson.Gson;
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
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.hisp.dhis.android.core.common.FeatureType;
import org.jetbrains.annotations.NotNull;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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
    public static final String POLYGON_DATA = "polygon_data";
    public static final String MULTI_POLYGON_DATA = "multi_polygon_data";
    public static final String LONGITUDE = "longitude";
    private TextView latLon;
    private Style style;
    private FloatingActionButton save;
    private static List<CustomMark> points = new ArrayList<>();
    private static int index = 0;
    private List<Source> markers = new ArrayList<>();
    private List<Layer> layers = new ArrayList<>();
    private FeatureType location_type;
    private static final String LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA";

    @NonNull
    public static Intent create(@NonNull Activity activity, FeatureType locationType) {
        Intent intent = new Intent(activity, MapSelectorActivity.class);
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_map_selector);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        FloatingActionButton fab = findViewById(R.id.fab);
        location_type = FeatureType.valueOf(
                getIntent().getStringExtra(LOCATION_TYPE_EXTRA)
        );
        if (location_type == FeatureType.NONE) {finish();}
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (points.size() <= index) {
                    points.add(new CustomMark(new ArrayList<>(), UUID.randomUUID().toString()));
                    points.get(index).marker.add(new ArrayList<>());
                }
                Point point = Point.fromLngLat(map.getCameraPosition().target.getLongitude(),
                        map.getCameraPosition().target.getLatitude());
                points.get(index).marker.get(0).add(point);
                if (location_type == FeatureType.POINT) {
                    if (map != null && map.getCameraPosition().target != null) {
                        setList();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                    return;
                }
                addPoint(point);

            }
        });
        save = findViewById(R.id.save);
        save.setVisibility(location_type == FeatureType.POLYGON ? View.VISIBLE : View.GONE);
        save.setOnClickListener(v -> {
            setList();
        });
        FloatingActionButton add = findViewById(R.id.add);
        add.setVisibility(location_type == FeatureType.MULTI_POLYGON ? View.VISIBLE : View.GONE);
        add.setOnClickListener(v -> {
            index++;
            updateVector();
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

    private void setList() {
        Intent data = new Intent();
        if (location_type == FeatureType.POLYGON) {
            List<Point> returnList = new ArrayList<>();
            for (CustomMark lst: points) {
                returnList.addAll(lst.marker.get(0));
            }
            data.putExtra(LATITUDE, String.valueOf(returnList.get(0).latitude()));
            data.putExtra(LONGITUDE, String.valueOf(returnList.get(0).longitude()));
            data.putExtra(POLYGON_DATA, new Gson().toJson(returnList));
        } else if (location_type == FeatureType.POINT) {
            data.putExtra(LATITUDE, String.valueOf(map.getCameraPosition().target.getLatitude()));
            data.putExtra(LONGITUDE, String.valueOf(map.getCameraPosition().target.getLongitude()));
        } else if (location_type == FeatureType.MULTI_POLYGON) {
            List<List<Point>> returnList = new ArrayList<>();
            for (CustomMark lst: points) {
                returnList.addAll(lst.marker);
            }
            data.putExtra(LATITUDE, String.valueOf(returnList.get(0).get(0).latitude()));
            data.putExtra(LONGITUDE, String.valueOf(returnList.get(0).get(0).longitude()));
            data.putExtra(MULTI_POLYGON_DATA, new Gson().toJson(returnList));
        }

        setResult(RESULT_OK, data);
        finish();
    }

    public void addPoint(Point point) {
        String uuid = UUID.randomUUID().toString();
        style.addImage(uuid,
                BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource(uuid, Feature.fromGeometry(
                point));
        markers.add(geoJsonSource);
        style.addSource(geoJsonSource);

        SymbolLayer symbolLayer = new SymbolLayer(uuid, uuid);
        symbolLayer.withProperties(
                PropertyFactory.iconImage(uuid)
        );
        layers.add(symbolLayer);
        style.addLayer(symbolLayer);
    }

    public void updateVector() {
        for (Source s: markers) {
            style.removeSource(s);
        }
        for (Layer l: layers) {
            style.removeLayer(l);
        }
        markers.clear();
        layers.clear();
        for (CustomMark lst: points) {
            List<List<Point>> list = new ArrayList<>();
            for (List<Point> pointsList: lst.marker) {
                if (pointsList.size() == 1) {
                    pointsList.add(pointsList.get(0));
                }
                if (pointsList.get(0).longitude() != pointsList.get(pointsList.size() - 1).longitude()
                        && pointsList.get(0).latitude() != pointsList.get(pointsList.size() - 1).latitude()) {
                    pointsList.add(pointsList.get(0));
                }
                list.add(pointsList);
            }
            if (style.getSource(lst.uuid) == null) {
                style.addSource(new GeoJsonSource(lst.uuid, Polygon.fromLngLats(list)));
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                style.addLayer(new FillLayer(lst.uuid, lst.uuid).withProperties(
                        fillColor(color))
                );
            } else {
                ((GeoJsonSource) style.getSource(lst.uuid)).setGeoJson(Polygon.fromLngLats(list));
            }
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

class CustomMark {
    List<List<Point>> marker;
    String uuid;

    CustomMark(List<List<Point>> marker, String uuid) {
        this.marker = marker;
        this.uuid = uuid;
    }
}

