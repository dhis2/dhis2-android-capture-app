package org.dhis2.usescases.syncManager;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorViewHolder extends RecyclerView.ViewHolder {

    private final ItemErrorDialogBinding binding;
    private final ObservableBoolean sharing;
    private final FlowableProcessor<Pair<Boolean, ErrorMessageModel>> processor;

    public ErrorViewHolder(@NonNull ItemErrorDialogBinding binding, ObservableBoolean sharing, FlowableProcessor<Pair<Boolean, ErrorMessageModel>> processor) {
        super(binding.getRoot());
        this.binding = binding;
        this.sharing = sharing;
        this.processor = processor;
    }

    public void bind(ErrorMessageModel errorMessageModel) {
        binding.setSharing(sharing);
        binding.errorCode.setText(String.valueOf(errorMessageModel.getErrorCode()));
        binding.errorDate.setText(errorMessageModel.getFormattedDate());
        binding.errorDescription.setText(errorMessageModel.getErrorDescription());
        binding.errorMessage.setText(errorMessageModel.getErrorMessage());
        binding.image.setVisibility(errorMessageModel.getErrorDescription() != null ? View.VISIBLE : View.GONE);
        binding.image.setOnClickListener(view -> binding.errorDescription.setVisibility(binding.errorDescription.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
        binding.selected.setOnCheckedChangeListener((buttonView, isChecked) -> processor.onNext(Pair.create(isChecked, errorMessageModel)));
    }


}
