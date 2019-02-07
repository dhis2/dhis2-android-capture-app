package org.dhis2.usescases.datasets.datasetDetail;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemDatasetBinding;

import java.util.ArrayList;
import java.util.List;

public class DataSetDetailAdapter extends RecyclerView.Adapter<DataSetDetailViewHolder> {

    private DataSetDetailContract.Presenter presenter;
    private List<DataSetDetailModel> datasets;

    public DataSetDetailAdapter(DataSetDetailContract.Presenter presenter) {
        this.presenter = presenter;
        this.datasets = new ArrayList<>();
    }

    @NonNull
    @Override
    public DataSetDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDatasetBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_dataset, parent, false);

        return new DataSetDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataSetDetailViewHolder holder, int position) {
        DataSetDetailModel dataSetModel = datasets.get(position);
        holder.bind(presenter, dataSetModel);
    }

    @Override
    public int getItemCount() {
        return datasets.size();
    }

    public void setDatasets(List<DataSetDetailModel> datasets){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DataSetDiffCallback(this.datasets, datasets));
        this.datasets.clear();
        this.datasets.addAll(datasets);
        diffResult.dispatchUpdatesTo(this);
    }
}
