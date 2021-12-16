package org.dhis2.uicomponents.map.layer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.R
import org.dhis2.databinding.BasemapItemBinding

class BasemapAdapter(val mapLayerManager: MapLayerManager) :
    ListAdapter<BaseMap, BaseMapHolder>(object : DiffUtil.ItemCallback<BaseMap>() {
        override fun areItemsTheSame(oldItem: BaseMap, newItem: BaseMap): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: BaseMap, newItem: BaseMap): Boolean {
            return oldItem == newItem
        }
    }) {
    private val currentStyle = ObservableField<BaseMapType>(BaseMapType.STREET)

    init {
        submitList(
            arrayListOf(
                BaseMap(
                    BaseMapType.STREET,
                    R.string.dialog_layer_street_base_map,
                    R.drawable.street_map_preview
                ),
                BaseMap(
                    BaseMapType.SATELLITE,
                    R.string.dialog_layer_satellite_base_map,
                    R.drawable.satellite_map_preview
                )
            )
        )

        when (mapLayerManager.currentStyle) {
            Style.MAPBOX_STREETS -> currentStyle.set(BaseMapType.STREET)
            Style.SATELLITE_STREETS -> currentStyle.set(BaseMapType.SATELLITE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMapHolder {
        return BaseMapHolder(
            BasemapItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                currentSelectedStyle = currentStyle
            }
        ) {
            mapLayerManager.changeStyle(it)
            currentStyle.set(it)
        }
    }

    override fun onBindViewHolder(holder: BaseMapHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
