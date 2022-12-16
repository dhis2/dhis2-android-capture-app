package org.dhis2.maps.layer.basemaps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.maps.databinding.BasemapItemBinding
import org.dhis2.maps.layer.MapLayerManager

class BasemapAdapter(val mapLayerManager: MapLayerManager) :
    ListAdapter<BaseMap, BaseMapHolder>(object : DiffUtil.ItemCallback<BaseMap>() {
        override fun areItemsTheSame(oldItem: BaseMap, newItem: BaseMap): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: BaseMap, newItem: BaseMap): Boolean {
            return oldItem == newItem
        }
    }) {
    private val currentStyle = ObservableInt(mapLayerManager.currentStylePosition)

    init {
        submitList(mapLayerManager.baseMapManager.getBaseMaps())
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
