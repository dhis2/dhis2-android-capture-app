package org.dhis2.usescases.reservedValue;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemReservedValueBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class ReservedValueAdapter extends RecyclerView.Adapter<ReservedValueViewHolder> {

    private ReservedValueContracts.ReservedValuePresenter presenter;
    private List<ReservedValueModel> dataElements;

    public ReservedValueAdapter(ReservedValueContracts.ReservedValuePresenter presenter) {
        this.presenter = presenter;
        dataElements = new ArrayList<>();
    }

    @NonNull
    @Override
    public ReservedValueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ItemReservedValueBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_reserved_value, viewGroup, false);
        return new ReservedValueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservedValueViewHolder holder, int position) {
        holder.bind(presenter, dataElements.get(position));
    }

    @Override
    public int getItemCount() {
        return dataElements.size();
    }

    public void setDataElements(List<ReservedValueModel> list) {
        dataElements = list;
        notifyDataSetChanged();
    }
}
