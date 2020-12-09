package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.jetbrains.annotations.NotNull;
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

    public void bind(FieldUiModel uiModel, int position, FieldItemCallback callback, boolean isFocused) {
        FieldViewModel viewModel = (FieldViewModel) uiModel;
        viewModel.setCallback(new FieldUiModel.Callback() {

            @Override
            public void onNext() {
                callback.onNext(position);
            }

            @Override
            public void showDialog(@NotNull String title, String message) {
                callback.onShowDialog(title, message);
            }

            @Override
            public void onClick() {
                callback.onItemClick(position);
            }
        });

        if (isFocused) {
            viewModel.onActivate();
        } else {
            viewModel.onDeactivate();
        }

        binding.setVariable(BR.item, viewModel);
        binding.executePendingBindings();
    }

    public interface FieldItemCallback {

        void onNext(int position);

        void onShowDialog(String title, @Nullable String message);

        void onItemClick(int position);
    }
}
