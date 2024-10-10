package org.dhis2.maps.model

import androidx.compose.runtime.Stable
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.maps.MapView
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

@Stable
data class MapSelectorScreenActions(
    val onBackClicked: () -> Unit,
    val loadMap: (MapView) -> Unit,
    val configurePolygonInfoRecycler: (RecyclerView) -> Unit,
    val onClearLocation: () -> Unit,
    val onSearchLocation: (String) -> Unit,
    val onLocationSelected: (LocationItemModel) -> Unit,
    val onSearchCaptureMode: () -> Unit,
    val onButtonMode: () -> Unit,
    val onSearchOnAreaClick: () -> Unit,
    val onMyLocationButtonClick: () -> Unit,
    val onDoneButtonClick: () -> Unit,
)
