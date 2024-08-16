package org.dhis2.maps.geometry.polygon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Polygon
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.R
import org.dhis2.maps.databinding.ItemPolygonFullBinding

class PolygonAdapter(
    private val onAddPolygonPoint: (List<Double>) -> Unit,
    private val onRemovePolygonPoint: (index: Int, List<Double>) -> Unit,
) : ListAdapter<List<Double>, PolygonAdapter.Holder>(
    object : DiffUtil.ItemCallback<List<Double>>() {
        override fun areItemsTheSame(
            oldItem: List<Double>,
            newItem: List<Double>,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: List<Double>,
            newItem: List<Double>,
        ): Boolean {
            return oldItem == newItem
        }
    },
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemPolygonFullBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_polygon_full,
            parent,
            false,
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1, position)
    }

    fun updateWithFeatureCollection(featureCollection: FeatureCollection?) {
        val data = mutableListOf<List<Double>>()
        featureCollection?.features()?.filter { it.geometry() is Polygon }?.forEach { feature ->
            (feature.geometry() as Polygon).coordinates().forEach { points ->
                points.forEach {
                    data.add(it.coordinates())
                }
            }
        }
        data.add(listOf(-1.0, -1.0))
    }

    inner class Holder(val binding: ItemPolygonFullBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(point: List<Double>, isLast: Boolean, index: Int) {
            binding.let {
                it.isLast = isLast
                it.coordinateValue = if (isLast) {
                    ""
                } else {
                    "${point[0].truncate()}, ${point[1].truncate()}"
                }
                it.addPolygonButton.setOnClickListener {
                    onAddPolygonPoint(point)
                }
                it.removePolygonButton.setOnClickListener {
                    onRemovePolygonPoint(index, point)
                }
            }
        }
    }
}
