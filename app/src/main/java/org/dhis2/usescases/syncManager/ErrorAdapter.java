package org.dhis2.usescases.syncManager;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorAdapter extends RecyclerView.Adapter<ErrorViewHolder> {

    private final List<ErrorMessageModel> data;
    private final ObservableBoolean sharing;
    private FlowableProcessor<Pair<Boolean, ErrorMessageModel>> processor;

    public ErrorAdapter(List<ErrorMessageModel> data, ObservableBoolean sharing) {
        this.processor = PublishProcessor.create();
        this.data = data;
        this.sharing = sharing;
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemErrorDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_error_dialog, viewGroup, false);
        return new ErrorViewHolder(binding, sharing, processor);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder errorViewHolder, int i) {
        errorViewHolder.bind(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    public FlowableProcessor<Pair<Boolean, ErrorMessageModel>> asFlowable() {
        return processor;
    }
}
