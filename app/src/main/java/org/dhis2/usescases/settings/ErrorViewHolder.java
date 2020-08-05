package org.dhis2.usescases.settings;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.usescases.settings.models.ErrorViewModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.maintenance.D2Error;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

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
        binding.selected.setOnCheckedChangeListener((buttonView, isChecked) -> processor.onNext(Pair.create(isChecked, errorMessageModel)));
    }
}
