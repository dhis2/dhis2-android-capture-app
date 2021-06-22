package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.RecyclerViewUiEvents;
import org.dhis2.form.ui.intent.FormIntent;
import org.jetbrains.annotations.NotNull;

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
        FieldUiModel.Callback itemCallback = new FieldUiModel.Callback() {
            @Override
            public void recyclerViewUiEvents(@NotNull RecyclerViewUiEvents uiEvent) {
                callback.recyclerViewEvent(uiEvent);
            }

            @Override
            public void intent(@NotNull FormIntent intent) {
                callback.intent(intent);
            }

            @Override
            public void onNext() {
                callback.onNext(getLayoutPosition());
            }
        };
        uiModel.setCallback(itemCallback);

        binding.setVariable(BR.item, uiModel);
        binding.executePendingBindings();
    }

    public interface FieldItemCallback {
        void intent(@NotNull FormIntent intent);

        void recyclerViewEvent(@NotNull RecyclerViewUiEvents uiEvent);

        void onNext(int layoutPosition);
    }
}
