package org.dhis2.maps.geometry.polygon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ItemPolygonFullBinding
import org.dhis2.maps.model.MapData
import org.maplibre.geojson.Polygon

class PolygonAdapter(
    private val onAddPolygonPoint: (List<Double>) -> Unit,
    private val onRemovePolygonPoint: (index: Int, List<Double>) -> Unit,
) : ListAdapter<List<Double>, PolygonAdapter.Holder>(
        object : DiffUtil.ItemCallback<List<Double>>() {
            override fun areItemsTheSame(
                oldItem: List<Double>,
                newItem: List<Double>,
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: List<Double>,
                newItem: List<Double>,
            ): Boolean = oldItem == newItem
        },
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): Holder {
        val binding: ItemPolygonFullBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_polygon_full,
                parent,
                false,
            )
        return Holder(binding)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    fun updateWithFeatureCollection(mapData: MapData) {
        val data = mutableListOf<List<Double>>()
        mapData.featureCollection
            .features()
            ?.filter { it.geometry() is Polygon }
            ?.forEach { feature ->
                (feature.geometry() as Polygon).coordinates().forEach { points ->
                    points.forEach {
                        data.add(it.coordinates())
                    }
                }
            }
        submitList(data)
    }

    inner class Holder(
        val binding: ItemPolygonFullBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(point: List<Double>) {
            binding.let {
                it.coordinateValue = "${point[0].truncate()}, ${point[1].truncate()}"
                it.addPolygonButton.setOnClickListener {
                    onAddPolygonPoint(point)
                }
                it.removePolygonButton.setOnClickListener {
                    val currentPosition = bindingAdapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        onRemovePolygonPoint(currentPosition, point)
                    }
                }
            }
        }
    }
}
