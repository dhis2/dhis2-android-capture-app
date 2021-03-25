package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.jetbrains.annotations.Nullable;

public class FormViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding binding;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        ImageView fieldSelected = binding.getRoot().findViewById(R.id.fieldSelected);
        if (fieldSelected != null) {
            ViewExtensionsKt.clipWithAllRoundedCorners(fieldSelected, ExtensionsKt.getDp(2));
        }
    }

    public void bind(FieldUiModel uiModel, FieldItemCallback callback) {
        FieldViewModel viewModel = (FieldViewModel) uiModel;
        viewModel.setCallback(callback::onShowDialog);
        viewModel.setOnNextCallback(() -> callback.onNext(getLayoutPosition()));

        binding.setVariable(BR.item, viewModel);
        binding.executePendingBindings();
    }

    public interface FieldItemCallback {
        void onShowDialog(String title, @Nullable String message);
        void onNext(int layoutPosition);
    }
}
