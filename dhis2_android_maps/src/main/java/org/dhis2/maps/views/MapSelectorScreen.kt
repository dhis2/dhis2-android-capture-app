package org.dhis2.maps.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.location.AccuracyIndicator
import org.dhis2.maps.model.AccuracyRange
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LocationBar
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItem
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItemIcon
import org.hisp.dhis.mobile.ui.designsystem.component.SearchBarMode
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun MapSelectorScreen(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    mapSelectorViewModel: MapSelectorViewModel,
    onBackClicked: () -> Unit,
    onMapDataUpdated: (FeatureCollection) -> Unit,
    onLocationButtonClicked: () -> Unit,
    loadMap: (MapView) -> Unit,
    onDoneClicked: (result: String?) -> Unit,
    configurePolygonInfoRecycler: (RecyclerView) -> Unit,
) {
    val useTwoPaneLayout = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.MEDIUM -> false
        WindowWidthSizeClass.COMPACT -> false
        WindowWidthSizeClass.EXPANDED -> true
        else -> false
    }

    if (useTwoPaneLayout) {
        TwoPaneMapSelector(
            mapSelectorViewModel,
            onBackClicked,
            onMapDataUpdated,
            onLocationButtonClicked,
            loadMap,
            onDoneClicked,
            configurePolygonInfoRecycler,
        )
    } else {
        SinglePaneMapSelector(
            mapSelectorViewModel,
            onBackClicked,
            onMapDataUpdated,
            onLocationButtonClicked,
            loadMap,
            onDoneClicked,
            configurePolygonInfoRecycler,
        )
    }
}

@Composable
fun SinglePaneMapSelector(
    mapSelectorViewModel: MapSelectorViewModel,
    onBackClicked: () -> Unit,
    onMapDataUpdated: (FeatureCollection) -> Unit,
    onLocationButtonClicked: () -> Unit,
    loadMap: (MapView) -> Unit,
    onDoneClicked: (result: String?) -> Unit,
    configurePolygonInfoRecycler: (RecyclerView) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        SearchBar(mapSelectorViewModel, onBackClicked)

        Map(
            modifier = Modifier
                .weight(1f),
            mapSelectorViewModel = mapSelectorViewModel,
            onMapDataUpdated = onMapDataUpdated,
            onLocationButtonClicked = onLocationButtonClicked,
            loadMap = loadMap,
        )

        LocationInfoContent(mapSelectorViewModel, configurePolygonInfoRecycler)

        DoneButton(mapSelectorViewModel, onDoneClicked)
    }
}

@Composable
private fun TwoPaneMapSelector(
    mapSelectorViewModel: MapSelectorViewModel,
    onBackClicked: () -> Unit,
    onMapDataUpdated: (FeatureCollection) -> Unit,
    onLocationButtonClicked: () -> Unit,
    loadMap: (MapView) -> Unit,
    onDoneClicked: (result: String?) -> Unit,
    configurePolygonInfoRecycler: (RecyclerView) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .fillMaxSize()
                .padding(top = 56.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            SearchBar(mapSelectorViewModel, onBackClicked)

            LocationInfoContent(mapSelectorViewModel, configurePolygonInfoRecycler)

            DoneButton(mapSelectorViewModel, onDoneClicked)
        }

        Column(Modifier.weight(0.7f)) {
            Map(
                modifier = Modifier
                    .weight(1f),
                mapSelectorViewModel = mapSelectorViewModel,
                onMapDataUpdated = onMapDataUpdated,
                onLocationButtonClicked = onLocationButtonClicked,
                loadMap = loadMap,
            )
        }
    }
}

@Composable
private fun SearchBar(mapSelectorViewModel: MapSelectorViewModel, onBackClicked: () -> Unit) {
    val locationItems by mapSelectorViewModel.locationItems.collectAsState(emptyList())

    LocationBar(
        currentResults = locationItems,
        onBackClicked = onBackClicked,
        onClearLocation = mapSelectorViewModel::onClearSearchClicked,
        onSearchLocation = mapSelectorViewModel::onSearchLocation,
        onLocationSelected = mapSelectorViewModel::onLocationSelected,
        onModeChanged = {
            if (it == SearchBarMode.SEARCH) {
                mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.SEARCH)
            }
        },
    )
}

@Composable
private fun LocationInfoContent(
    mapSelectorViewModel: MapSelectorViewModel,
    configurePolygonInfoRecycler: (RecyclerView) -> Unit,
) {
    val selectedLocation by mapSelectorViewModel.selectedLocation.collectAsState()

    val captureMode by mapSelectorViewModel.captureMode.collectAsState()

    when {
        captureMode.isNone() && selectedLocation is SelectedLocation.ManualResult -> {
            with(selectedLocation as SelectedLocation.ManualResult) {
                LocationItem(
                    locationItemModel = LocationItemModel.SearchResult(
                        searchedTitle = "Selected location",
                        searchedSubtitle = "Lat: ${selectedLocation.latitude.truncate()}, Lon: ${selectedLocation.longitude.truncate()}",
                        searchedLatitude = selectedLocation.latitude,
                        searchedLongitude = selectedLocation.longitude,
                    ),
                    icon = {
                        LocationItemIcon(
                            icon = Icons.Outlined.Place,
                            tintedColor = SurfaceColor.Primary,
                            bgColor = SurfaceColor.PrimaryContainer,
                        )
                    },
                ) { }
            }
        }

        captureMode.isGps() -> {
            val accuracy by mapSelectorViewModel.accuracyRange.collectAsState(
                AccuracyRange.None(),
            )
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                AccuracyIndicator(accuracyRange = accuracy)
            }
        }

        captureMode.isManual() && selectedLocation is SelectedLocation.ManualResult -> {
            LocationItem(
                locationItemModel = LocationItemModel.SearchResult(
                    searchedTitle = "Selected location",
                    searchedSubtitle = "Lat: ${selectedLocation.latitude.truncate()}, Lon: ${selectedLocation.longitude.truncate()}",
                    searchedLatitude = selectedLocation.latitude,
                    searchedLongitude = selectedLocation.longitude,
                ),
                icon = {
                    LocationItemIcon(
                        icon = Icons.Outlined.Place,
                        tintedColor = SurfaceColor.Primary,
                        bgColor = SurfaceColor.PrimaryContainer,
                    )
                },
            ) { }
        }

        captureMode.isSearch() -> {
            if (selectedLocation is SelectedLocation.None) {
                LocationItem(
                    locationItemModel = LocationItemModel.SearchResult(
                        searchedTitle = "Select location",
                        searchedSubtitle = "by clicking on it",
                        searchedLatitude = 0.0,
                        searchedLongitude = 0.0,
                    ),
                    icon = {
                        LocationItemIcon(
                            icon = Icons.Outlined.TouchApp,
                            tintedColor = TextColor.OnWarningContainer,
                            bgColor = SurfaceColor.WarningContainer,
                        )
                    },
                ) {
                }
            } else if (selectedLocation is SelectedLocation.SearchResult) {
                with(selectedLocation as SelectedLocation.SearchResult) {
                    LocationItem(
                        locationItemModel = LocationItemModel.SearchResult(
                            searchedTitle = title,
                            searchedSubtitle = address,
                            searchedLatitude = latitude,
                            searchedLongitude = longitude,
                        ),
                        icon = {
                            LocationItemIcon(
                                icon = Icons.Outlined.Place,
                                tintedColor = SurfaceColor.Primary,
                                bgColor = SurfaceColor.PrimaryContainer,
                            )
                        },
                    ) { }
                }
            }
        }
    }
}

@Composable
private fun Map(
    modifier: Modifier = Modifier,
    mapSelectorViewModel: MapSelectorViewModel,
    onMapDataUpdated: (FeatureCollection) -> Unit,
    onLocationButtonClicked: () -> Unit,
    loadMap: (MapView) -> Unit,
) {
    val mapData by mapSelectorViewModel.mapFeatures.collectAsState()
    val searchOnThisAreaVisible by mapSelectorViewModel.searchOnThisAreaVisible.collectAsState()

    LaunchedEffect(mapData) {
        onMapDataUpdated(mapData)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
    ) {
        MapScreen(
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spacedBy(8.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    SwipeToChangeLocationInfo(modifier = Modifier.weight(1f))

                    IconButton(
                        style = IconButtonStyle.TONAL,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_my_location),
                                contentDescription = "",
                            )
                        },
                        onClick = {
                            mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.GPS)
                            onLocationButtonClicked()
                        },
                    )
                }
            },
            map = {
                AndroidView(factory = { context ->
                    val map = MapView(context)
                    loadMap(map)
                    map
                }) {
                }
            },
            extraContent = {
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 60.dp),
                    visible = searchOnThisAreaVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
                    exit = scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
                ) {
                    SearchInAreaButton(
                        mapSelectorViewModel = mapSelectorViewModel,
                    )
                }
            },
        )
    }
}

@Composable
private fun SwipeToChangeLocationInfo(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.6f),
                shape = Shape.Full,
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Swipe,
            contentDescription = "",
            tint = SurfaceColor.Primary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = "Swipe to change location",
            style = MaterialTheme.typography.labelLarge,
            color = TextColor.OnSurface,
        )
    }
}

@Composable
private fun SearchInAreaButton(
    mapSelectorViewModel: MapSelectorViewModel,
) {
    val scope = rememberCoroutineScope()
    Button(
        style = ButtonStyle.TONAL,
        text = "Search on this area",
        onClick = {
            scope.launch {
                mapSelectorViewModel.performLocationSearch()
            }
        },
    )
}

@Composable
private fun DoneButton(
    mapSelectorViewModel: MapSelectorViewModel,
    onDoneClicked: (result: String?) -> Unit,
) {
    val enabled by mapSelectorViewModel.canSave.collectAsState(false)
    Button(
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        text = stringResource(R.string.done),
        onClick = { mapSelectorViewModel.onSaveCurrentGeometry(onDoneClicked) },
    )
}
