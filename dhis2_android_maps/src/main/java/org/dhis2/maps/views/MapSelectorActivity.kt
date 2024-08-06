package org.dhis2.maps.views

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.engine.LocationEngineDefault
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.location.engine.MapboxFusedLocationEngineImpl
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.commons.bindings.clipWithAllRoundedCorners
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.locationprovider.LocationProviderImpl
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ActivityMapSelectorBinding
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.polygon.PolygonAdapter
import org.dhis2.maps.location.AccuracyIndicator
import org.dhis2.maps.location.LOCATION_FASTEST_INTERVAL
import org.dhis2.maps.location.LOCATION_INTERVAL
import org.dhis2.maps.location.MapActivityLocationCallback
import org.dhis2.maps.managers.DefaultMapManager
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.ui.theme.Dhis2Theme
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import androidx.compose.ui.unit.dp as DP

class MapSelectorActivity :
    AppCompatActivity(),
    MapActivityLocationCallback.OnLocationChanged,
    MapboxMap.OnMapClickListener {

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initLocationUpdates()
            }
        }

    private val locationProvider = LocationProviderImpl(this)

    override fun onLocationChanged(latLng: LatLng, accuracy: Float) {
        mapSelectorViewModel.onNewLocation(latLng, accuracy)
    }

    private val locationListener = LocationListener { location ->
        mapSelectorViewModel.onNewLocation(
            LatLng(
                location.latitude,
                location.longitude,
            ),
            location.accuracy,
        )
    }

    private val locationCallback = MapActivityLocationCallback(this)

    private var fieldUid: String? = null

    private lateinit var locationType: FeatureType
    lateinit var binding: ActivityMapSelectorBinding

    var init: Boolean = false

    private var initialCoordinates: String? = null
    private lateinit var mapManager: DefaultMapManager

    private val mapSelectorViewModel: MapSelectorViewModel by viewModels<MapSelectorViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapSelectorViewModel(
                    featureType = locationType,
                    initialCoordinates = initialCoordinates,
                    mapStyleConfig = MapStyleConfiguration(D2Manager.getD2()),
                ) as T
            }
        }
    }

    private val polygonAdapter = PolygonAdapter(
        onAddPolygonPoint = { mapSelectorViewModel.addPointToPolygon(it) },
        onRemovePolygonPoint = { index, _ -> mapSelectorViewModel.removePointFromPolygon(index) },
    )

    private lateinit var mapboxLocationProvider: MapboxFusedLocationEngineImpl

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_selector)
        binding.back.setOnClickListener { finish() }
        locationType = intent.getStringExtra(LOCATION_TYPE_EXTRA)?.let { featureName ->
            FeatureType.valueOf(featureName)
        } ?: FeatureType.POINT

        fieldUid = intent.getStringExtra(FIELD_UID)
        initialCoordinates = intent.getStringExtra(INITIAL_GEOMETRY_COORDINATES)

        binding.mapView.clipWithAllRoundedCorners(8.dp)

        mapManager = DefaultMapManager(binding.mapView, locationType)
        mapboxLocationProvider = MapboxFusedLocationEngineImpl(this)
        mapSelectorViewModel.featureCollection.asLiveData().observe(this) { featureCollection ->
            mapManager.update(
                featureCollection = featureCollection,
                boundingBox = GetBoundingBox().getEnclosingBoundingBox(
                    featureCollection.features()?.getLatLngPointList() ?: emptyList(),
                ),
            )
            if (locationType == FeatureType.POLYGON) {
                polygonAdapter.updateWithFeatureCollection(featureCollection)
            }
        }

        lifecycle.addObserver(mapManager)
        mapManager.onMapClickListener = this
        mapManager.init(
            mapStyles = mapSelectorViewModel.fetchMapStyles(),
            onInitializationFinished = {
                mapSelectorViewModel.init()
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    initLocationUpdates()
                } else {
                    requestLocationPermission.launch(permission.ACCESS_FINE_LOCATION)
                }
            },
            onMissingPermission = { permissionsManager ->
                if (locationProvider.hasLocationEnabled()) {
                    permissionsManager?.requestLocationPermissions(this)
                } else {
                    LocationSettingLauncher.requestEnableLocationSetting(this)
                }
            },
        )

        binding.mapPositionButton.setOnClickListener {
            if (locationProvider.hasLocationEnabled()) {
                mapManager.centerCameraOnMyPosition { permissionManager ->
                    permissionManager?.requestLocationPermissions(this)
                }
            } else {
                LocationSettingLauncher.requestEnableLocationSetting(this)
            }
        }

        binding.saveButton.setContent {
            Button(
                style = ButtonStyle.FILLED,
                text = resources.getString(R.string.done),
                onClick = {
                    mapSelectorViewModel.onSaveCurrentGeometry { result ->
                        runOnUiThread {
                            result?.let(::finishResult)
                        }
                    }
                },
            )
        }

        if (locationType == FeatureType.POINT) {
            binding.accuracyIndicator.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@MapSelectorActivity))
                setContent {
                    Dhis2Theme {
                        val accuracy by mapSelectorViewModel.accuracyRange.collectAsState(
                            AccuracyRange.None(),
                        )
                        Box(
                            Modifier
                                .height(64.DP)
                                .padding(horizontal = 16.DP),
                            contentAlignment = Alignment.Center,
                        ) {
                            AccuracyIndicator(accuracyRange = accuracy)
                        }
                    }
                }
            }
        } else {
            binding.recycler.adapter = polygonAdapter
            binding.recycler.layoutManager = GridLayoutManager(this, 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapManager.permissionsManager?.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
        )
    }

    @RequiresPermission(permission.ACCESS_FINE_LOCATION)
    private fun initLocationUpdates() {
        mapboxLocationProvider.requestLocationUpdates(
            LocationEngineRequest.Builder(LOCATION_INTERVAL)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build(),
            locationListener,
            Looper.myLooper(),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationEngineDefault.getDefaultLocationEngine(this)
            .removeLocationUpdates(locationCallback)
        mapboxLocationProvider.removeLocationUpdates(locationListener)
    }

    companion object {
        const val DATA_EXTRA = "data_extra"
        const val LOCATION_TYPE_EXTRA = "LOCATION_TYPE_EXTRA"
        const val INITIAL_GEOMETRY_COORDINATES = "INITIAL_DATA"
        const val FIELD_UID = "FIELD_UID_EXTRA"

        fun create(activity: Context, locationType: FeatureType): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            return intent
        }

        fun create(activity: Context, locationType: FeatureType, initialData: String?): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            if (initialData != null) {
                intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
            }
            return intent
        }

        fun create(
            activity: Context,
            fieldUid: String,
            locationType: FeatureType,
            initialData: String?,
        ): Intent {
            val intent = Intent(activity, MapSelectorActivity::class.java)
            intent.putExtra(FIELD_UID, fieldUid)
            intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
            if (initialData != null) {
                intent.putExtra(INITIAL_GEOMETRY_COORDINATES, initialData)
            }
            return intent
        }
    }

    private fun finishResult(value: String) {
        val intent = Intent()
        intent.putExtra(FIELD_UID, fieldUid)
        intent.putExtra(DATA_EXTRA, value)
        intent.putExtra(LOCATION_TYPE_EXTRA, locationType.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onMapClick(point: LatLng): Boolean {
        mapSelectorViewModel.updateSelectedGeometry(point)
        return true
    }
}
