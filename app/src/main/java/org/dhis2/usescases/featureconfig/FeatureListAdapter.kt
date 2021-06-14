package org.dhis2.usescases.featureconfig

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.FeatureItemBinding

class FeatureListAdapter(private val viewModel: FeatureConfigViewModel) :
    ListAdapter<Feature, FeatureListAdapter.ViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FeatureItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = getItem(position)
        holder.bind(viewModel, feature)
    }

    inner class ViewHolder(val binding: FeatureItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: FeatureConfigViewModel, feature: Feature) {
            binding.feature = feature
            binding.viewModel = viewModel
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Feature>() {
    override fun areItemsTheSame(oldItem: Feature, newItem: Feature): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Feature, newItem: Feature): Boolean {
        return oldItem == newItem
    }
}
