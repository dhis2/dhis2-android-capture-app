package org.dhis2.data.forms.dataentry.fields.image;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormImageBinding;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageRow implements Row<ImageHolder, ImageViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;

    public ImageRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor,
                    String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public ImageHolder onCreate(@NonNull ViewGroup parent) {
        FormImageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_image, parent, false);
        return new ImageHolder(binding, processor, null);
    }

    public ImageHolder onCreate(@NonNull ViewGroup parent, int count, ObservableField<String> imageSelector, String sectionRendering) {

        FormImageBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_image, parent, false);

        Integer height = null;
        Integer parentHeight = parent.getHeight();
        if (sectionRendering != null && sectionRendering.equals(ProgramStageSectionRenderingType.SEQUENTIAL.name())) {
            height = parentHeight / (count > 2 ? 3 : count);
        } else if (sectionRendering != null && sectionRendering.equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            height = parentHeight / (count / 2 + 1);
        }

        View rootView = binding.getRoot();
        if (height != null) {
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = height;
            rootView.setLayoutParams(layoutParams);
        }

        return new ImageHolder(binding, processor, imageSelector);
    }

    @Override
    public void onBind(@NonNull ImageHolder viewHolder, @NonNull ImageViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
