package org.dhis2.android_maps.geometry.point

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.android_maps.R
import org.dhis2.android_maps.databinding.ItemPointGeoBinding

class PointAdapter(
    val viewModel: PointViewModel
) : RecyclerView.Adapter<PointAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemPointGeoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_point_geo, parent, false
        )
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind()
    }

    inner class Holder(val binding: ItemPointGeoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewModel = viewModel
        }
    }
}
