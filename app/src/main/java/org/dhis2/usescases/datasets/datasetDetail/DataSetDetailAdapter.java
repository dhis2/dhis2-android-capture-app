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

    private DataSetDetailPresenter presenter;
    private List<DataSetDetailModel> dataSets;

    public DataSetDetailAdapter(DataSetDetailPresenter presenter) {
        this.presenter = presenter;
        this.dataSets = new ArrayList<>();
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
        DataSetDetailModel dataSetModel = dataSets.get(position);
        holder.bind(presenter, dataSetModel);
    }

    @Override
    public int getItemCount() {
        return dataSets.size();
    }

    public void setDataSets(List<DataSetDetailModel> dataSets){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DataSetDiffCallback(this.dataSets, dataSets));
        this.dataSets.clear();
        this.dataSets.addAll(dataSets);
        diffResult.dispatchUpdatesTo(this);
    }
}
