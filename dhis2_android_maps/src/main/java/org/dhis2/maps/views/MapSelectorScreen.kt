package org.dhis2.maps.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.location.AccuracyIndicator
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapSelectorScreenActions
import org.dhis2.maps.model.MapSelectorScreenState
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LocationBar
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItem
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItemIcon
import org.hisp.dhis.mobile.ui.designsystem.component.OnSearchAction
import org.hisp.dhis.mobile.ui.designsystem.component.SearchBarMode
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun MapSelectorScreen(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
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
            screenState,
            mapSelectorScreenActions,
        )
    } else {
        SinglePaneMapSelector(
            screenState,
            mapSelectorScreenActions,
        )
    }
}

@Composable
fun SinglePaneMapSelector(
    screenState: MapSelectorScreenState,
    screenActions: MapSelectorScreenActions,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        SearchBar(
            locationItems = screenState.locationItems,
            onBackClicked = screenActions.onBackClicked,
            onClearLocation = screenActions.onClearLocation,
            onSearchLocation = screenActions.onSearchLocation,
            onLocationSelected = screenActions.onLocationSelected,
            onSearchCaptureMode = screenActions.onSearchCaptureMode,
            onButtonMode = screenActions.onButtonMode,
        )

        Map(
            modifier = Modifier
                .weight(1f),
            searchOnThisAreaVisible = screenState.searchOnAreaVisible,
            captureMode = screenState.captureMode,
            selectedLocation = screenState.selectedLocation,
            loadMap = screenActions.loadMap,
            onSearchOnAreaClick = screenActions.onSearchOnAreaClick,
            onMyLocationButtonClicked = screenActions.onMyLocationButtonClick,
        )

        LocationInfoContent(
            screenState.selectedLocation,
            screenState.captureMode,
            screenState.displayPolygonInfo,
            screenState.accuracyRange,
            screenActions.configurePolygonInfoRecycler,
        )

        DoneButton(
            enabled = screenState.doneButtonEnabled,
            onDoneButtonClicked = screenActions.onDoneButtonClick,
        )
    }
}

@Composable
private fun TwoPaneMapSelector(
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
            SearchBar(
                locationItems = screenState.locationItems,
                onBackClicked = screenActions.onBackClicked,
                onClearLocation = screenActions.onClearLocation,
                onSearchLocation = screenActions.onSearchLocation,
                onLocationSelected = screenActions.onLocationSelected,
                onSearchCaptureMode = screenActions.onSearchCaptureMode,
                onButtonMode = {
                    // no-op
                },
            )

            LocationInfoContent(
                selectedLocation = screenState.selectedLocation,
                captureMode = screenState.captureMode,
                displayPolygonInfo = screenState.displayPolygonInfo,
                accuracyRange = screenState.accuracyRange,
                configurePolygonInfoRecycler = screenActions.configurePolygonInfoRecycler,
            )

            DoneButton(
                enabled = screenState.doneButtonEnabled,
                onDoneButtonClicked = screenActions.onDoneButtonClick,
            )
        }

        Column(Modifier.weight(0.7f)) {
            Map(
                modifier = Modifier
                    .weight(1f),
                captureMode = screenState.captureMode,
                selectedLocation = screenState.selectedLocation,
                searchOnThisAreaVisible = screenState.searchOnAreaVisible,
                loadMap = screenActions.loadMap,
                onSearchOnAreaClick = screenActions.onSearchOnAreaClick,
                onMyLocationButtonClicked = screenActions.onMyLocationButtonClick,
            )
        }
    }
}

@Composable
private fun SearchBar(
    locationItems: List<LocationItemModel>,
    onBackClicked: () -> Unit,
    onClearLocation: () -> Unit,
    onSearchLocation: (String) -> Unit,
    onLocationSelected: (LocationItemModel) -> Unit,
    onSearchCaptureMode: () -> Unit,
    onButtonMode: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var timeLeft by remember(locationItems) { mutableIntStateOf(1000) }
    LaunchedEffect(timeLeft) {
        while (timeLeft > 0) {
            delay(100)
            timeLeft -= 100
        }
    }

    LocationBar(
        currentResults = locationItems,
        searchAction = OnSearchAction.OnOneItemSelect,
        onBackClicked = onBackClicked,
        onClearLocation = onClearLocation,
        onSearchLocation = {
            timeLeft = 1000
            onSearchLocation(it)
        },
        onLocationSelected = {
            scope.launch {
                delay(1000)
                if (timeLeft == 0) {
                    onLocationSelected(it)
                }
            }
        },
        onModeChanged = {
            when (it) {
                SearchBarMode.BUTTON -> onButtonMode()
                SearchBarMode.SEARCH -> onSearchCaptureMode()
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

        (captureMode.isManual() or captureMode.isNone()) && selectedLocation is SelectedLocation.ManualResult -> {
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

        captureMode.isSwipe() -> {
            LocationItem(
                locationItemModel = LocationItemModel.SearchResult(
                    searchedTitle = stringResource(R.string.drop_to_select),
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
                        icon = Icons.Outlined.TouchApp,
                        tintedColor = TextColor.OnWarningContainer,
                        bgColor = SurfaceColor.WarningContainer,
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
    captureMode: MapSelectorViewModel.CaptureMode,
    selectedLocation: SelectedLocation,
    searchOnThisAreaVisible: Boolean,
    loadMap: (MapView) -> Unit,
    onSearchOnAreaClick: () -> Unit,
    onMyLocationButtonClicked: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val locationState by mapSelectorViewModel.locationState.collectAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        MapScreen(
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spacedBy(8.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    SwipeToChangeLocationInfo(modifier = Modifier.weight(1f))

                    LocationIcon(
                        locationState = locationState,
                        onLocationButtonClicked = {
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
                        onClick = onSearchOnAreaClick,
                    )
                }
            },
        )

        DraggableSelectedIcon(
            captureMode = captureMode,
            selectedLocation = selectedLocation,
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
    enabled: Boolean,
    onDoneButtonClicked: () -> Unit,
) {
    Button(
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        text = stringResource(R.string.done),
        onClick = onDoneButtonClicked,
    )
}

@Composable
private fun DraggableSelectedIcon(
    captureMode: MapSelectorViewModel.CaptureMode,
    selectedLocation: SelectedLocation,
) {
    val density = LocalDensity.current

    if (selectedLocation !is SelectedLocation.None) {
        var heightOffset by remember {
            mutableStateOf(0.dp)
        }
        val iconOffset by animateDpAsState(
            if (captureMode.isSwipe()) {
                (-15).dp
            } else {
                0.dp
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "offset",
        )
        Box(
            modifier = Modifier
                .offset(y = heightOffset)
                .onGloballyPositioned {
                    heightOffset = -with(density) { it.size.height.toDp() } / 2
                }
                .graphicsLayer { clip = false },
        ) {
            Icon(
                modifier = Modifier.offset(y = iconOffset),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_map_pin_selected_no_shadow),
                contentDescription = "",
                tint = Color.Unspecified,
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_map_pin_shadow),
                contentDescription = "",
                tint = Color.Unspecified,
            )
        }
    }
}
