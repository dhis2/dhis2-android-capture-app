package org.dhis2.usescases.datasets.datasetDetail;

import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;


import org.dhis2.databinding.ItemDatasetBinding;

import io.reactivex.disposables.CompositeDisposable;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder{

    private ItemDatasetBinding binding;

    public DataSetDetailViewHolder(ItemDatasetBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(DataSetDetailPresenter presenter, DataSetDetailModel dataSet) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataSet);
        binding.executePendingBindings();

        binding.syncIcon.setOnClickListener(view ->  presenter.onSyncIconClick(dataSet));

        itemView.setOnClickListener(view -> presenter.openDataSet(dataSet));
    }
}
