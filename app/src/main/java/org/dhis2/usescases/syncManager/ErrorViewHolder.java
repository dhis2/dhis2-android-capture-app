package org.dhis2.usescases.syncManager;

import android.view.View;

import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.maintenance.D2Error;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorViewHolder extends RecyclerView.ViewHolder {

    private final ItemErrorDialogBinding binding;
    private final ObservableBoolean sharing;
    private final FlowableProcessor<Pair<Boolean, D2Error>> processor;

    public ErrorViewHolder(@NonNull ItemErrorDialogBinding binding, ObservableBoolean sharing, FlowableProcessor<Pair<Boolean, D2Error>> processor) {
        super(binding.getRoot());
        this.binding = binding;
        this.sharing = sharing;
        this.processor = processor;
    }

    public void bind(D2Error errorMessageModel) {
        binding.setSharing(sharing);
        binding.errorCode.setText(String.valueOf(errorMessageModel.httpErrorCode()));
        binding.errorDate.setText(DateUtils.uiDateFormat().format(errorMessageModel.created()));
        binding.errorDescription.setText(errorMessageModel.errorDescription());
        binding.errorMessage.setText(String.format("%s : %s", errorMessageModel.errorComponent().name(), errorMessageModel.errorCode()));
        binding.image.setVisibility(!isEmpty(errorMessageModel.errorDescription()) ? View.VISIBLE : View.GONE);
        binding.image.setOnClickListener(view -> binding.errorDescription.setVisibility(binding.errorDescription.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
        binding.selected.setOnCheckedChangeListener((buttonView, isChecked) -> processor.onNext(Pair.create(isChecked, errorMessageModel)));
    }


}
