package org.dhis2.usescases.reservedValue;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import org.dhis2.R;
import org.dhis2.databinding.ItemReservedValueBinding;

public class ReservedValueAdapter extends RecyclerView.Adapter<ReservedValueViewHolder> {

    private ReservedValuePresenter presenter;
    private List<ReservedValueModel> dataElements;

    public ReservedValueAdapter(ReservedValuePresenter presenter) {
        this.presenter = presenter;
        dataElements = new ArrayList<>();
    }

    @NonNull
    @Override
    public ReservedValueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ItemReservedValueBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_reserved_value, viewGroup, false);
        return new ReservedValueViewHolder(binding, presenter);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservedValueViewHolder holder, int position) {
        holder.bind(presenter, dataElements.get(position));
    }

    @Override
    public int getItemCount() {
        return dataElements.size();
    }

    public void setDataElements(List<ReservedValueModel> list){
        dataElements = list;
        notifyDataSetChanged();
    }
}
