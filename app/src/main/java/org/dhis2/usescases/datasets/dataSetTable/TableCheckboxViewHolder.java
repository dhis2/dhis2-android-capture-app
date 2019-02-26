package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.databinding.ItemDatasetBinding;
import org.dhis2.databinding.ItemTableCheckboxBinding;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailContract;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel;

import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;

public class TableCheckboxViewHolder extends RecyclerView.ViewHolder{

    private ItemTableCheckboxBinding binding;

    public TableCheckboxViewHolder(ItemTableCheckboxBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(String title) {
        binding.setTitle(title);
    }
}
