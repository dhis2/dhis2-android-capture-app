package org.dhis2.data.forms.dataentry.fields.image;

import android.view.View;

import androidx.databinding.ObservableField;

import org.dhis2.Bindings.Bindings;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormImageBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageHolder extends FormViewHolder {

    public static final String NAME_CODE_DELIMITATOR = "_op_";

    private final FormImageBinding binding;
    private final ObservableField<String> currentSelector;
    private boolean isEditable;

    private ImageViewModel model;

    public ImageHolder(FormImageBinding mBinding, FlowableProcessor<RowAction> processor, ObservableField<String> imageSelector) {
        super(mBinding);
        this.binding = mBinding;
        this.currentSelector = imageSelector;

        itemView.setOnClickListener(v -> {

            if (isEditable) {
                String value;
                String[] uids = model.uid().split("\\.");
                String[] labelAndCode = model.label().split(NAME_CODE_DELIMITATOR);
                String label = labelAndCode[0];
                String code = labelAndCode[1];
                if (imageSelector.get().equals(label)) {
                    value = null;
                    currentSelector.set("");
                } else {
                    value = code;
                    currentSelector.set(label);
                }

                processor.onNext(RowAction.create(uids[0], value,getAdapterPosition()));
            }
        });

    }

    public void update(ImageViewModel viewModel) {
        this.model = viewModel;
        if(currentSelector.get() != null && viewModel.value() == null){
            currentSelector.set("");
        }

        this.isEditable = viewModel.editable();
        descriptionText = viewModel.description();
        String[] labelAndCode = viewModel.label().split(NAME_CODE_DELIMITATOR);
        String labelName = labelAndCode[0];
        String code = labelAndCode[1];
        label = new StringBuilder(labelName);
        if (viewModel.mandatory())
            label.append("*");
        binding.setLabel(label.toString());
        binding.setCurrentSelection(currentSelector);

        String[] uids = viewModel.uid().split("\\.");
        Bindings.setObjectStyle(binding.icon, itemView, viewModel.objectStyle());
        Bindings.setObjectStyle(binding.label, itemView, viewModel.objectStyle());

        if (viewModel.value() != null && !viewModel.value().equals(currentSelector.get()))
            currentSelector.set(viewModel.value());
        if (viewModel.warning() != null) {
            binding.errorMessage.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(viewModel.warning());
        } else if (viewModel.error() != null) {
            binding.errorMessage.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(viewModel.error());
        } else {
            binding.errorMessage.setVisibility(View.GONE);
            binding.errorMessage.setText(null);
        }
    }
}
