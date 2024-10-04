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
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.location.AccuracyIndicator
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapSelectorScreenActions
import org.dhis2.maps.model.MapSelectorScreenState
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
    screenState: MapSelectorScreenState,
    mapSelectorScreenActions: MapSelectorScreenActions,
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
            screenState,
            mapSelectorScreenActions,
        )
    } else {
        SinglePaneMapSelector(
            mapSelectorViewModel,
            screenState,
            mapSelectorScreenActions,
        )
    }
}

@Composable
fun SinglePaneMapSelector(
    mapSelectorViewModel: MapSelectorViewModel,
    screenState: MapSelectorScreenState,
    screenActions: MapSelectorScreenActions,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        SearchBar(screenState.locationItems, mapSelectorViewModel, screenActions.onBackClicked)

        Map(
            modifier = Modifier
                .weight(1f),
            mapSelectorViewModel = mapSelectorViewModel,
            searchOnThisAreaVisible = screenState.searchOnAreaVisible,
            onLocationButtonClicked = screenActions.onLocationButtonClicked,
            loadMap = screenActions.loadMap,
        )

        LocationInfoContent(
            screenState.selectedLocation,
            screenState.captureMode,
            screenState.displayPolygonInfo,
            screenState.accuracyRange,
            screenActions.configurePolygonInfoRecycler,
        )

        DoneButton(
            mapSelectorViewModel = mapSelectorViewModel,
            enabled = screenState.doneButtonEnabled,
            onDoneClicked = screenActions.onDoneClicked,
        )
    }
}

@Composable
private fun TwoPaneMapSelector(
    mapSelectorViewModel: MapSelectorViewModel,
    screenState: MapSelectorScreenState,
    screenActions: MapSelectorScreenActions,
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
            SearchBar(screenState.locationItems, mapSelectorViewModel, screenActions.onBackClicked)

            LocationInfoContent(
                selectedLocation = screenState.selectedLocation,
                captureMode = screenState.captureMode,
                displayPolygonInfo = screenState.displayPolygonInfo,
                accuracyRange = screenState.accuracyRange,
                configurePolygonInfoRecycler = screenActions.configurePolygonInfoRecycler,
            )

            DoneButton(
                mapSelectorViewModel,
                enabled = screenState.doneButtonEnabled,
                onDoneClicked = screenActions.onDoneClicked,
            )
        }

        Column(Modifier.weight(0.7f)) {
            Map(
                modifier = Modifier
                    .weight(1f),
                mapSelectorViewModel = mapSelectorViewModel,
                searchOnThisAreaVisible = screenState.searchOnAreaVisible,
                onLocationButtonClicked = screenActions.onLocationButtonClicked,
                loadMap = screenActions.loadMap,
            )
        }
    }
}

@Composable
private fun SearchBar(
    locationItems: List<LocationItemModel>,
    mapSelectorViewModel: MapSelectorViewModel,
    onBackClicked: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    LocationBar(
        currentResults = locationItems,
        onBackClicked = onBackClicked,
        onClearLocation = mapSelectorViewModel::onClearSearchClicked,
        onSearchLocation = mapSelectorViewModel::onSearchLocation,
        onLocationSelected = mapSelectorViewModel::onLocationSelected,
        onModeChanged = {
            scope.launch {
                if (it == SearchBarMode.SEARCH) {
                    mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.SEARCH)
                }
            }
        },
    )
}

@Composable
private fun LocationInfoContent(
    selectedLocation: SelectedLocation,
    captureMode: MapSelectorViewModel.CaptureMode,
    displayPolygonInfo: Boolean,
    accuracyRange: AccuracyRange,
    configurePolygonInfoRecycler: (RecyclerView) -> Unit,
) {
    when {
        displayPolygonInfo -> {
            AndroidView(
                factory = { context ->
                    RecyclerView(context)
                },
                update = configurePolygonInfoRecycler,
            )
        }

        captureMode.isNone() && selectedLocation is SelectedLocation.ManualResult -> {
            LocationItem(
                locationItemModel = LocationItemModel.SearchResult(
                    searchedTitle = stringResource(R.string.selected_location),
                    searchedSubtitle = stringResource(
                        R.string.latitude_longitude,
                        selectedLocation.latitude.truncate(),
                        selectedLocation.longitude.truncate(),
                    ),
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

        captureMode.isGps() -> {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                AccuracyIndicator(accuracyRange = accuracyRange)
            }
        }

        captureMode.isManual() && selectedLocation is SelectedLocation.ManualResult -> {
            LocationItem(
                locationItemModel = LocationItemModel.SearchResult(
                    searchedTitle = stringResource(R.string.selected_location),
                    searchedSubtitle = stringResource(
                        R.string.latitude_longitude,
                        selectedLocation.latitude.truncate(),
                        selectedLocation.longitude.truncate(),
                    ),
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
                        searchedTitle = stringResource(R.string.select_location_title),
                        searchedSubtitle = stringResource(R.string.selet_location_subtitle),
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
                with(selectedLocation) {
                    LocationItem(
                        locationItemModel = LocationItemModel.SearchResult(
                            searchedTitle = title
                                .takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.selected_location),
                            searchedSubtitle = address
                                .takeIf { it.isNotBlank() }
                                ?: stringResource(
                                    R.string.latitude_longitude,
                                    selectedLocation.latitude.truncate(),
                                    selectedLocation.longitude.truncate(),
                                ),
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
    searchOnThisAreaVisible: Boolean,
    onLocationButtonClicked: () -> Unit,
    loadMap: (MapView) -> Unit,
) {
    val scope = rememberCoroutineScope()
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
                            scope.launch {
                                mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.GPS)
                                onLocationButtonClicked()
                            }
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
                        onClick = mapSelectorViewModel::onSearchOnAreaClick,
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
            text = stringResource(R.string.swipe_to_change_location),
            style = MaterialTheme.typography.labelLarge,
            color = TextColor.OnSurface,
        )
    }
}

@Composable
private fun SearchInAreaButton(
    onClick: () -> Unit = {},
) {
    Button(
        style = ButtonStyle.TONAL,
        text = stringResource(R.string.search_on_this_area),
        onClick = onClick,
    )
}

@Composable
private fun DoneButton(
    mapSelectorViewModel: MapSelectorViewModel,
    enabled: Boolean,
    onDoneClicked: (result: String?) -> Unit,
) {
    Button(
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        text = stringResource(R.string.done),
        onClick = { mapSelectorViewModel.onSaveCurrentGeometry(onDoneClicked) },
    )
}
