package org.dhis2.usescases.datasets.dataSetTable;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemTableCheckboxBinding;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TableCheckboxAdapter extends RecyclerView.Adapter<TableCheckboxViewHolder> {

    private List<String> tables;
    private DataSetTableContract.Presenter presenter;
    private int selectedPosition;
    public TableCheckboxAdapter(DataSetTableContract.Presenter presenter){
        this.tables = new ArrayList<>();
        this.presenter = presenter;
    }

    public void swapData(List<String> tables){
        this.tables = new ArrayList<>(tables);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableCheckboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTableCheckboxBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_table_checkbox, parent, false);

        return new TableCheckboxViewHolder(binding, presenter);
    }

    @Override
    public void onBindViewHolder(@NonNull TableCheckboxViewHolder holder, int position) {
        holder.bind(tables.get(position), position, selectedPosition);
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    public void setSelectedPosition(int selectedPosition){
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }
}
