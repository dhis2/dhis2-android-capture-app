package org.dhis2.usescases.datasets.dataSetTable;

import android.content.Context;
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

    private final Context context;
    private int tablesCount;
    private DataSetTableContract.Presenter presenter;
    private int selectedPosition;

    public TableCheckboxAdapter(DataSetTableContract.Presenter presenter, Context context){
        this.tablesCount = 0;
        this.presenter = presenter;
        this.context = context;
    }

    public void swapData(int count){
        this.tablesCount = count;
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
        holder.bind(context.getString(R.string.table) + " " + (position + 1) , position, selectedPosition);
    }

    @Override
    public int getItemCount() {
        return tablesCount;
    }

    public void setSelectedPosition(int selectedPosition){
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }
}
