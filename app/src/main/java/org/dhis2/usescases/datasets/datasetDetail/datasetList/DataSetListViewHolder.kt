package org.dhis2.usescases.datasets.datasetDetail.datasetList

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemDatasetBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel

class DataSetListViewHolder(private val binding: ItemDatasetBinding) : RecyclerView.ViewHolder(
    binding.root,
) {
    fun bind(dataSet: DataSetDetailModel, viewModel: DataSetListViewModel) {
        binding.viewModel = viewModel
        binding.dataset = dataSet
        binding.executePendingBindings()
    }
}
