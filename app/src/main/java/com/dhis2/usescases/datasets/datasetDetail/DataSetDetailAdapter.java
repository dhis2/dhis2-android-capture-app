package com.dhis2.usescases.datasets.datasetDetail;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemDatasetBinding;

import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;

public class DataSetDetailAdapter extends RecyclerView.Adapter<DataSetDetailViewHolder> {

    private DataSetDetailContract.Presenter presenter;
    private List<DataSetModel> datasets;

    @NonNull
    @Override
    public DataSetDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDatasetBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_dataset, parent, false);

        return new DataSetDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataSetDetailViewHolder holder, int position) {
        DataSetModel dataSetModel = datasets.get(position);

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
