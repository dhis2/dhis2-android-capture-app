package org.dhis2.maps.layer.basemaps

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.maps.databinding.BasemapItemBinding

class BaseMapHolder(
    val binding: BasemapItemBinding,
    private val onItemSelected: (stylePosition: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(baseMap: BaseMap) {
        binding.apply {
            itemStyle = bindingAdapterPosition
            baseMapImage.setImageResource(baseMap.basemapImage)
            basemapName.setText(baseMap.basemapName)
        }
        itemView.setOnClickListener { onItemSelected(bindingAdapterPosition) }
    }
}
