package org.dhis2.maps.layer.basemaps

import android.graphics.Color
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.maps.R
import org.dhis2.maps.databinding.BasemapItemBinding

class BaseMapHolder(
    val binding: BasemapItemBinding,
    private val onItemSelected: (stylePosition: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(baseMap: BaseMap) {
        binding.apply {
            itemStyle = bindingAdapterPosition
            if (baseMap.basemapImage != null) {
                baseMapImage.setImageDrawable(baseMap.basemapImage)
                baseMapImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                baseMapImage.setBackgroundColor(Color.GRAY)
                baseMapImage.setImageResource(R.drawable.unknown_base_map)
                baseMapImage.scaleType = ImageView.ScaleType.FIT_CENTER
            }
            basemapName.text = baseMap.basemapName
        }
        itemView.setOnClickListener { onItemSelected(bindingAdapterPosition) }
    }
}
