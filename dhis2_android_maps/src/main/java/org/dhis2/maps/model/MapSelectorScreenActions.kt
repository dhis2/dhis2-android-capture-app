package org.dhis2.maps.model

import androidx.compose.runtime.Stable
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.maps.MapView

@Stable
data class MapSelectorScreenActions(
    val onBackClicked: () -> Unit,
    val onMapDataUpdated: (MapData) -> Unit,
    val onLocationButtonClicked: () -> Unit,
    val loadMap: (MapView) -> Unit,
    val onDoneClicked: (result: String?) -> Unit,
    val configurePolygonInfoRecycler: (RecyclerView) -> Unit,
)
