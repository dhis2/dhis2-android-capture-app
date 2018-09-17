package org.dhis2.data.forms.dataentry.fields.image;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
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
    private final boolean isBackgroundTransparent;
    private final String renderType;

    public ImageRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public ImageHolder onCreate(@NonNull ViewGroup parent) {
        FormImageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_image, parent, false);
        return new ImageHolder(binding, processor, isBackgroundTransparent, renderType, null);
    }

    public ImageHolder onCreate(@NonNull ViewGroup parent, int count) {
        FormImageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_image, parent, false);
        Integer height = null;
        if (renderType.equals(ProgramStageSectionRenderingType.SEQUENTIAL.name())) {
            height = parent.getMeasuredHeight() / (count > 2 ? 3 : count);
        } else if (renderType.equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            height = parent.getMeasuredHeight() / (count > 2 ? 2 : count);
        }

        View rootView = binding.getRoot();
        if (height != null) {
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = height;
            rootView.setLayoutParams(layoutParams);
        }

        return new ImageHolder(binding, processor, isBackgroundTransparent, renderType, rootView);
    }

    @Override
    public void onBind(@NonNull ImageHolder viewHolder, @NonNull ImageViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull ImageHolder viewHolder) {
        viewHolder.dispose();
    }
}
