package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.BR;
import org.dhis2.databinding.ItemDatasetBinding;

import androidx.recyclerview.widget.RecyclerView;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder{

    private ItemDatasetBinding binding;

    public DataSetDetailViewHolder(ItemDatasetBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(DataSetDetailContract.Presenter presenter, DataSetDetailModel dataset) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataset);
        binding.executePendingBindings();
    }
}
