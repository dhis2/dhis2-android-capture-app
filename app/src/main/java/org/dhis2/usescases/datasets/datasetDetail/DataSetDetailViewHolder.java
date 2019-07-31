package org.dhis2.usescases.datasets.datasetDetail;

import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;


import org.dhis2.databinding.ItemDatasetBinding;

import io.reactivex.disposables.CompositeDisposable;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder{

    private ItemDatasetBinding binding;
    private CompositeDisposable disposable;

    public DataSetDetailViewHolder(ItemDatasetBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        disposable = new CompositeDisposable();
    }

    public void bind(DataSetDetailContract.Presenter presenter, DataSetDetailModel dataset) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataset);
        binding.executePendingBindings();

        binding.syncIcon.setOnClickListener(view ->  presenter.onSyncIconClick(dataset.orgUnitUid(), dataset.catOptionComboUid(), dataset.periodId()));

        itemView.setOnClickListener(view -> presenter.onDataSetClick(
                dataset.orgUnitUid(),
                dataset.nameOrgUnit(),
                dataset.periodId(),
                dataset.periodType(),
                dataset.namePeriod(),
                dataset.catOptionComboUid()));
    }
}
