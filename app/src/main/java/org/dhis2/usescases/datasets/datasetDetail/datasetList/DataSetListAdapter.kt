package org.dhis2.usescases.datasets.datasetDetail.datasetList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.databinding.ItemDatasetBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel

class DataSetListAdapter(val viewModel: DataSetListViewModel) :
    ListAdapter<DataSetDetailModel, DataSetListViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataSetListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDatasetBinding.inflate(inflater, parent, false)
        return DataSetListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataSetListViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<DataSetDetailModel>() {
    override fun areItemsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: DataSetDetailModel,
        newItem: DataSetDetailModel
    ): Boolean {
        return oldItem == newItem
    }
}
