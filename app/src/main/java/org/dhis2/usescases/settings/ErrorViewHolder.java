package org.dhis2.usescases.settings;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorViewHolder extends RecyclerView.ViewHolder {

    private final ItemErrorDialogBinding binding;
    private final ObservableBoolean sharing;
    private final FlowableProcessor<Pair<Boolean, TrackerImportConflict>> processor;

    public ErrorViewHolder(@NonNull ItemErrorDialogBinding binding, ObservableBoolean sharing,
                           FlowableProcessor<Pair<Boolean, TrackerImportConflict>> processor) {
        super(binding.getRoot());
        this.binding = binding;
        this.sharing = sharing;
        this.processor = processor;
    }

    public void bind(TrackerImportConflict errorMessageModel) {
        binding.setSharing(sharing);
        switch (errorMessageModel.status()) {
            case ERROR:
                binding.errorCode.setImageResource(R.drawable.red_circle);
                break;
            case WARNING:
                binding.errorCode.setImageResource(R.drawable.yellow_circle);
                break;
            case SUCCESS:
                binding.errorCode.setImageResource(R.drawable.green_circle);
                break;
            default:
                binding.errorCode.setImageResource(0);
                break;

        }
        binding.errorDate.setText(DateUtils.dateTimeFormat().format(errorMessageModel.created()));
        binding.errorMessage.setText(errorMessageModel.conflict());
        binding.selected.setOnCheckedChangeListener((buttonView, isChecked) -> processor.onNext(Pair.create(isChecked, errorMessageModel)));
    }
}
