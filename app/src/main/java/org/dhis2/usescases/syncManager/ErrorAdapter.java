package org.dhis2.usescases.syncManager;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorAdapter extends RecyclerView.Adapter<ErrorViewHolder> {

    private final List<ErrorMessageModel> data;

    public ErrorAdapter(List<ErrorMessageModel> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemErrorDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_error_dialog, viewGroup, false);
        return new ErrorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder errorViewHolder, int i) {
        errorViewHolder.bind(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
