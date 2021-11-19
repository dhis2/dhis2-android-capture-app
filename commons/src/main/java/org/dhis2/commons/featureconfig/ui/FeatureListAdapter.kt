package org.dhis2.commons.featureconfig.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.databinding.FeatureItemBinding
import org.dhis2.commons.featureconfig.model.FeatureState

class FeatureListAdapter(private val viewModel: FeatureConfigViewModel) :
    ListAdapter<FeatureState, FeatureListAdapter.ViewHolder>(ItemDiffCallback()) {

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
        fun bind(viewModel: FeatureConfigViewModel, featureState: FeatureState) {
            binding.featureState = featureState
            binding.viewModel = viewModel
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<FeatureState>() {
    override fun areItemsTheSame(oldItem: FeatureState, newItem: FeatureState): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FeatureState, newItem: FeatureState): Boolean {
        return oldItem == newItem
    }
}
