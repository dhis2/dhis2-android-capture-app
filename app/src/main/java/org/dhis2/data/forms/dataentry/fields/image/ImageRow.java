package org.dhis2.data.forms.dataentry.fields.image;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormImageBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageRow implements Row<ImageHolder, ImageViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;

    public ImageRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public ImageHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormImageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.custom_form_image, parent, false);
        binding.formImageView.setLayout();
        return new ImageHolder(binding);
    }

    @Override
    public void onBind(@NonNull ImageHolder viewHolder, @NonNull ImageViewModel viewModel) {
        viewHolder.bind(viewModel);
    }
}
