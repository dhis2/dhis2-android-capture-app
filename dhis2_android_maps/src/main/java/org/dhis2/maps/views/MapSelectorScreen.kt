package org.dhis2.maps.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.location.AccuracyIndicator
import org.dhis2.maps.location.LocationState
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapData
import org.dhis2.maps.model.MapSelectorScreenActions
import org.dhis2.maps.model.MapSelectorScreenState
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.LocationBar
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItem
import org.hisp.dhis.mobile.ui.designsystem.component.LocationItemIcon
import org.hisp.dhis.mobile.ui.designsystem.component.OnSearchAction
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.SearchBarMode
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.maplibre.android.maps.MapView
import org.maplibre.geojson.FeatureCollection

@Composable
fun MapSelectorScreen(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    screenState: MapSelectorScreenState,
    mapSelectorScreenActions: MapSelectorScreenActions,
) {
    val useTwoPaneLayout =
        when (windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.MEDIUM -> false
            WindowWidthSizeClass.COMPACT -> false
            WindowWidthSizeClass.EXPANDED -> true
            else -> false
        }

    Scaffold(
        modifier =
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { Box {} },
    ) { paddingValues ->
        if (useTwoPaneLayout && screenState.isManualCaptureEnabled && !screenState.displayPolygonInfo) {
            TwoPaneMapSelector(
                screenState,
                mapSelectorScreenActions,
                paddingValues,
            )
        } else {
            SinglePaneMapSelector(
                screenState,
                mapSelectorScreenActions,
                paddingValues,
            )
        }
    }
}

@Composable
fun SinglePaneMapSelector(
    screenState: MapSelectorScreenState,
    screenActions: MapSelectorScreenActions,
    paddingValues: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceBright),
    ) {
        if (!screenState.isManualCaptureEnabled || screenState.displayPolygonInfo) {
            MapTopBar(screenActions.onBackClicked)
        }

        Column(
            Modifier
                .padding(16.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            if (screenState.isManualCaptureEnabled && !screenState.displayPolygonInfo) {
                SearchBar(
                    searching = screenState.searching,
                    locationItems = screenState.locationItems,
                    searchBarActions =
                        SearchBarActions(
                            onBackClicked = screenActions.onBackClicked,
                            onClearLocation = screenActions.onClearLocation,
                            onSearchLocation = screenActions.onSearchLocation,
                            onLocationSelected = screenActions.onLocationSelected,
                            onSearchCaptureMode = screenActions.onSearchCaptureMode,
                            onButtonMode = screenActions.onButtonMode,
                        ),
                )
            }

            Map(
                modifier =
                    Modifier
                        .weight(1f),
                captureMode = screenState.captureMode,
                selectedLocation = screenState.selectedLocation,
                searchOnThisAreaVisible = screenState.searchOnAreaVisible,
                searching = screenState.searching,
                locationState = screenState.locationState,
                isManualCaptureEnabled = screenState.isManualCaptureEnabled,
                isPolygonMode = screenState.displayPolygonInfo,
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
}

@Composable
private fun TwoPaneMapSelector(
    screenState: MapSelectorScreenState,
    screenActions: MapSelectorScreenActions,
    paddingValues: PaddingValues,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 0.dp,
                    top = paddingValues.calculateTopPadding(),
                    end = 0.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                ).background(MaterialTheme.colorScheme.surfaceBright),
    ) {
        val localLayoutDirection = LocalLayoutDirection.current

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = 16.dp,
                        start = paddingValues.calculateStartPadding(localLayoutDirection) + 16.dp,
                        end = paddingValues.calculateEndPadding(localLayoutDirection) + 16.dp,
                        bottom = 16.dp,
                    ),
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .fillMaxSize(),
                verticalArrangement = spacedBy(16.dp),
            ) {
                if (!screenState.isManualCaptureEnabled || screenState.displayPolygonInfo) {
                    MapTopBar(screenActions.onBackClicked)
                }

                if (screenState.isManualCaptureEnabled && !screenState.displayPolygonInfo) {
                    SearchBar(
                        searching = screenState.searching,
                        locationItems = screenState.locationItems,
                        searchBarActions =
                            SearchBarActions(
                                onBackClicked = screenActions.onBackClicked,
                                onClearLocation = screenActions.onClearLocation,
                                onSearchLocation = screenActions.onSearchLocation,
                                onLocationSelected = screenActions.onLocationSelected,
                                onSearchCaptureMode = screenActions.onSearchCaptureMode,
                                onButtonMode = {
                                    // no-op
                                },
                            ),
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column {
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
                }
            }

            Column(Modifier.weight(0.7f)) {
                Map(
                    modifier =
                        Modifier
                            .weight(1f),
                    captureMode = screenState.captureMode,
                    selectedLocation = screenState.selectedLocation,
                    searchOnThisAreaVisible = screenState.searchOnAreaVisible,
                    searching = screenState.searching,
                    locationState = screenState.locationState,
                    isManualCaptureEnabled = screenState.isManualCaptureEnabled,
                    isPolygonMode = screenState.displayPolygonInfo,
                    loadMap = screenActions.loadMap,
                    onSearchOnAreaClick = screenActions.onSearchOnAreaClick,
                    onMyLocationButtonClicked = screenActions.onMyLocationButtonClick,
                )
            }
        }
    }
}

private data class SearchBarActions(
    val onBackClicked: () -> Unit,
    val onClearLocation: () -> Unit,
    val onSearchLocation: (String) -> Unit,
    val onLocationSelected: (LocationItemModel) -> Unit,
    val onSearchCaptureMode: () -> Unit,
    val onButtonMode: () -> Unit,
)

@Composable
private fun SearchBar(
    searching: Boolean,
    locationItems: List<LocationItemModel>,
    searchBarActions: SearchBarActions,
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
        onBackClicked = searchBarActions.onBackClicked,
        onClearLocation = searchBarActions.onClearLocation,
        onSearchLocation = {
            timeLeft = 1000
            searchBarActions.onSearchLocation(it)
        },
        searching = searching,
        onLocationSelected = {
            scope.launch {
                delay(1000)
                if (timeLeft == 0) {
                    searchBarActions.onLocationSelected(it)
                }
            }
        },
        onModeChanged = {
            when (it) {
                SearchBarMode.BUTTON -> searchBarActions.onButtonMode()
                SearchBarMode.SEARCH -> searchBarActions.onSearchCaptureMode()
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
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(64.dp),
    ) {
        when {
            displayPolygonInfo -> {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                    factory = { context ->
                        RecyclerView(context).also {
                            configurePolygonInfoRecycler(it)
                        }
                    },
                    update = {
                        // no-op
                    },
                )
            }

            (captureMode.isManual() or captureMode.isNone() || captureMode.isSearchManual()) &&
                selectedLocation is SelectedLocation.ManualResult -> {
                LocationItem(
                    locationItemModel =
                        LocationItemModel.SearchResult(
                            searchedTitle = stringResource(R.string.selected_location),
                            searchedSubtitle =
                                stringResource(
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

            captureMode.isSwipe() || captureMode.isSearchSwipe() -> {
                LocationItem(
                    locationItemModel =
                        LocationItemModel.SearchResult(
                            searchedTitle = stringResource(R.string.drop_to_select),
                            searchedSubtitle =
                                stringResource(
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
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AccuracyIndicator(accuracyRange = accuracyRange)
                }
            }

            captureMode.isSearch() || captureMode.isSearchManual() -> {
                if (selectedLocation is SelectedLocation.None) {
                    LocationItem(
                        locationItemModel =
                            LocationItemModel.SearchResult(
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
                            locationItemModel =
                                LocationItemModel.SearchResult(
                                    searchedTitle =
                                        title
                                            .takeIf { it.isNotBlank() }
                                            ?: stringResource(R.string.selected_location),
                                    searchedSubtitle =
                                        address
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
}

@Composable
private fun Map(
    modifier: Modifier = Modifier,
    captureMode: MapSelectorViewModel.CaptureMode,
    selectedLocation: SelectedLocation,
    searchOnThisAreaVisible: Boolean,
    searching: Boolean,
    locationState: LocationState,
    isManualCaptureEnabled: Boolean,
    isPolygonMode: Boolean,
    loadMap: (MapView) -> Unit,
    onSearchOnAreaClick: () -> Unit,
    onMyLocationButtonClicked: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceColor.ContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        MapScreen(
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isManualCaptureEnabled) spacedBy(8.dp) else Arrangement.End,
                    verticalAlignment = CenterVertically,
                ) {
                    if (isManualCaptureEnabled) {
                        SwipeToChangeLocationInfo(modifier = Modifier.weight(1f))
                    }

                    if (!isPolygonMode) {
                        LocationIcon(
                            locationState = locationState,
                            onLocationButtonClicked = onMyLocationButtonClicked,
                        )
                    }
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
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 60.dp),
                    visible = searchOnThisAreaVisible or searching,
                    enter =
                        scaleIn(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                        ),
                    exit =
                        scaleOut(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                        ),
                ) {
                    SearchInAreaButton(
                        searching = searching,
                        onClick = onSearchOnAreaClick,
                    )
                }
            },
        )

        if (!isPolygonMode) {
            DraggableSelectedIcon(
                captureMode = captureMode,
                selectedLocation = selectedLocation,
            )
        }
    }
}

@Composable
private fun SwipeToChangeLocationInfo(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.6f),
                    shape = Shape.Full,
                ).padding(8.dp),
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
    searching: Boolean,
    onClick: () -> Unit = {},
) {
    Button(
        style = ButtonStyle.TONAL,
        text = if (searching) "" else stringResource(R.string.search_on_this_area),
        icon =
            if (searching) {
                { ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL) }
            } else {
                null
            },
        paddingValues =
            PaddingValues(
                Spacing.Spacing24,
                Spacing.Spacing10,
                if (searching) Spacing.Spacing24 - Spacing.Spacing8 else Spacing.Spacing24,
                Spacing.Spacing10,
            ),
        onClick = { if (searching.not()) onClick() },
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
            if (captureMode.isSwipe() || captureMode.isSearchSwipe() || captureMode.isSearchPinClicked()) {
                (-15).dp
            } else {
                0.dp
            },
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            label = "offset",
        )
        Box(
            modifier =
                Modifier
                    .offset(y = heightOffset)
                    .onGloballyPositioned {
                        heightOffset = -with(density) { it.size.height.toDp() } / 2
                    }.graphicsLayer { clip = false },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapTopBar(onBackClicked: () -> Unit) {
    TopBar(
        navigationIcon = {
            IconButton(
                onClick = onBackClicked,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                    )
                },
            )
        },
        title = {
            Text(text = stringResource(R.string.select_location_title))
        },
        actions = {},
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceColor.ContainerLow,
            ),
    )
}

@Preview(device = "id:pixel_8a", showBackground = true)
@Composable
fun TestPortraitMap(
    @PreviewParameter(ScreenStateParamProvider::class) screenState: MapSelectorScreenState,
) {
    MapSelectorScreen(
        screenState = screenState,
        mapSelectorScreenActions = previewActions,
    )
}

@Preview(device = "id:pixel_tablet", showBackground = true)
@Composable
fun TestLandscapeMap(
    @PreviewParameter(ScreenStateParamProvider::class) screenState: MapSelectorScreenState,
) {
    Scaffold { paddingValues ->
        TwoPaneMapSelector(
            screenState = screenState,
            screenActions = previewActions,
            paddingValues = paddingValues,
        )
    }
}

private class ScreenStateParamProvider : PreviewParameterProvider<MapSelectorScreenState> {
    override val values: Sequence<MapSelectorScreenState>
        get() =
            sequenceOf(
                searchScreenState,
                gpsScreenState,
                gpsScreenState.copy(searching = true),
            )
}

private val searchScreenState =
    MapSelectorScreenState(
        mapData =
            MapData(
                featureCollection = FeatureCollection.fromFeatures(emptyList()),
                boundingBox = null,
            ),
        locationItems =
            listOf(
                LocationItemModel.StoredResult(
                    storedTitle = "title",
                    storedSubtitle = "subtitle",
                    storedLatitude = 0.0,
                    storedLongitude = 0.0,
                ),
                LocationItemModel.SearchResult(
                    searchedTitle = "title",
                    searchedSubtitle = "subtitle",
                    searchedLatitude = 0.0,
                    searchedLongitude = 0.0,
                ),
            ),
        selectedLocation = SelectedLocation.None(),
        captureMode = MapSelectorViewModel.CaptureMode.SEARCH,
        accuracyRange = AccuracyRange.None(),
        searchOnAreaVisible = true,
        displayPolygonInfo = false,
        locationState = LocationState.OFF,
        isManualCaptureEnabled = true,
        forcedLocationAccuracy = 10,
        lastGPSLocation = null,
        searching = false,
    )

private val gpsScreenState =
    MapSelectorScreenState(
        mapData =
            MapData(
                featureCollection = FeatureCollection.fromFeatures(emptyList()),
                boundingBox = null,
            ),
        locationItems = emptyList(),
        selectedLocation =
            SelectedLocation.GPSResult(
                selectedLatitude = 0.0,
                selectedLongitude = 0.0,
                accuracy = 5f,
            ),
        captureMode = MapSelectorViewModel.CaptureMode.GPS,
        accuracyRange = AccuracyRange.VeryGood(5),
        searchOnAreaVisible = true,
        displayPolygonInfo = false,
        locationState = LocationState.FIXED,
        isManualCaptureEnabled = true,
        forcedLocationAccuracy = 10,
        lastGPSLocation = null,
        searching = false,
    )

private val previewActions =
    MapSelectorScreenActions(
        onBackClicked = { },
        loadMap = {},
        configurePolygonInfoRecycler = {},
        onClearLocation = {},
        onSearchLocation = {},
        onLocationSelected = {},
        onSearchCaptureMode = {},
        onButtonMode = {},
        onSearchOnAreaClick = {},
        onMyLocationButtonClick = {},
        onDoneButtonClick = {},
    )
