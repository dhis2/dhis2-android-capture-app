package org.dhis2.usescases.settings;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.commons.date.DateUtils;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.usescases.settings.models.ErrorViewModel;

import io.reactivex.processors.FlowableProcessor;
import kotlin.Pair;

public class ErrorViewHolder extends RecyclerView.ViewHolder {

    private final ItemErrorDialogBinding binding;
    private final ObservableBoolean sharing;
    private final FlowableProcessor<Pair<Boolean, ErrorViewModel>> processor;

    public ErrorViewHolder(@NonNull ItemErrorDialogBinding binding, ObservableBoolean sharing,
                           FlowableProcessor<Pair<Boolean, ErrorViewModel>> processor) {
        super(binding.getRoot());
        this.binding = binding;
        this.sharing = sharing;
        this.processor = processor;
    }

    public void bind(ErrorViewModel errorMessageModel) {
        binding.setSharing(sharing);
        binding.errorCode.setText(String.valueOf(errorMessageModel.getErrorCode()));
        binding.errorDate.setText(DateUtils.dateTimeFormat().format(errorMessageModel.getCreationDate()));
        binding.errorMessage.setText(errorMessageModel.getErrorDescription());
        binding.errorComponent.setText(errorMessageModel.getErrorComponent());
        binding.selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            errorMessageModel.setSelected(isChecked);
            processor.onNext(new kotlin.Pair<>(isChecked, errorMessageModel));
        });
        binding.selected.setChecked(errorMessageModel.isSelected());
    }
}
