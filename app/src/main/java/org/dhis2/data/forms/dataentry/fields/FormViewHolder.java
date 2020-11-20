package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;


public abstract class FormViewHolder extends RecyclerView.ViewHolder {

    protected ViewDataBinding binding;
    protected ImageView fieldSelected;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.fieldSelected = binding.getRoot().findViewById(R.id.fieldSelected);
        if (fieldSelected != null) {
            ViewExtensionsKt.clipWithAllRoundedCorners(fieldSelected, ExtensionsKt.getDp(2));
        }
    }

    public void bind(FieldUiModel uiModel, int position, FieldItemCallback callback) {
        FieldViewModel viewModel = (FieldViewModel) uiModel;
        viewModel.setAdapterPosition(position);
        viewModel.setCallback(() -> callback.onNext(position));
        binding.setVariable(BR.item, viewModel);
        update(viewModel);
        binding.executePendingBindings();
    }

    protected abstract void update(FieldViewModel viewModel);

    public interface FieldItemCallback {
        void onNext(int position);
    }
}
