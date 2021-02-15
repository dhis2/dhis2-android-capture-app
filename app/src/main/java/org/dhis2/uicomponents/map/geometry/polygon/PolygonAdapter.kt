package org.dhis2.uicomponents.map.geometry.polygon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemPolygonFullBinding

class PolygonAdapter(
    val list: List<PolygonViewModel.PolygonPoint>,
    val viewModel: PolygonViewModel
) : RecyclerView.Adapter<PolygonAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemPolygonFullBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_polygon_full, parent, false
        )
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (holder.adapterPosition == list.size) {
            holder.bind(viewModel.createPolygonPoint())
        } else {
            holder.bind(list[holder.adapterPosition])
        }
    }

    inner class Holder(val binding: ItemPolygonFullBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(polygonPoint: PolygonViewModel.PolygonPoint) {
            binding.let {
                it.isLast = adapterPosition == list.size
                it.viewModel = viewModel
                it.point = polygonPoint
            }
        }
    }
}
