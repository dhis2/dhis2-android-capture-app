package org.dhis2.usescases.syncManager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.dhis2.databinding.ItemErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */

public class ErrorViewHolder extends RecyclerView.ViewHolder {

    private final ItemErrorDialogBinding binding;

    public ErrorViewHolder(@NonNull ItemErrorDialogBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ErrorMessageModel errorMessageModel) {
        binding.errorCode.setText(String.valueOf(errorMessageModel.getErrorCode()));
        binding.errorDate.setText(errorMessageModel.getFormattedDate());
        binding.errorDescription.setText(errorMessageModel.getErrorDescription());
        binding.errorMessage.setText(errorMessageModel.getErrorMessage());
        binding.image.setVisibility(errorMessageModel.getErrorDescription() != null ? View.VISIBLE : View.GONE);
        binding.image.setOnClickListener(view -> binding.errorDescription.setVisibility(binding.errorDescription.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
    }


}
