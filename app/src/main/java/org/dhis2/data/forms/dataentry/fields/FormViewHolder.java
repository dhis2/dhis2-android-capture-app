package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.utils.Constants;
import org.dhis2.utils.customviews.CustomDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;


public abstract class FormViewHolder extends RecyclerView.ViewHolder {

    protected ViewDataBinding binding;
    protected ImageView description;
    protected StringBuilder label;
    protected String descriptionText;
    protected String fieldUid;
    protected ObjectStyle objectStyle;
    protected ImageView fieldSelected;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.fieldSelected = binding.getRoot().findViewById(R.id.fieldSelected);
        if (fieldSelected != null) {
            ViewExtensionsKt.clipWithAllRoundedCorners(fieldSelected, ExtensionsKt.getDp(2));
        }
        if (description != null) {
            description.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            label.toString(),
                            descriptionText != null ? descriptionText : itemView.getContext().getString(R.string.empty_description),
                            itemView.getContext().getString(R.string.action_close),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }
    }

    public void bind(FieldUiModel uiModel, int position, FieldItemCallback callback) {
        FieldViewModel viewModel = (FieldViewModel) uiModel;
        viewModel.setAdapterPosition(position);
        viewModel.setCallback(() -> callback.onNext(position));
        fieldUid = viewModel.uid();
        label = new StringBuilder().append(viewModel.label());
        descriptionText = viewModel.description();
        binding.setVariable(BR.item, viewModel);
        update(viewModel);
        binding.executePendingBindings();
    }

    protected abstract void update(FieldViewModel viewModel);

    public interface FieldItemCallback {
        void onNext(int position);
    }
}
