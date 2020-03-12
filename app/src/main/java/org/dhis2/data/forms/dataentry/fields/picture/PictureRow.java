package org.dhis2.data.forms.dataentry.fields.picture;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormPictureBinding;
import org.dhis2.utils.customviews.PictureView;

import io.reactivex.processors.FlowableProcessor;

public class PictureRow implements Row<PictureHolder, PictureViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private final LayoutInflater inflater;
    private final boolean isBgTransparent;
    private final String renderType;
    private boolean isSearchMode = false;

    public PictureRow(@NonNull LayoutInflater layoutInflater,
                         @NonNull FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        this.isSearchMode = true;
    }

    public PictureRow(@NonNull LayoutInflater layoutInflater,
                      @NonNull FlowableProcessor<RowAction> processor,
                         boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public PictureHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormPictureBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_form_picture, parent, false);
        binding.formPictures.setIsBgTransparent(isBgTransparent);
        PictureView.OnIntentSelected onIntentSelected = (PictureView.OnIntentSelected) binding.formPictures.getContext();
        return new PictureHolder(onIntentSelected,
                binding, processor, isSearchMode);
    }

    @Override
    public void onBind(@NonNull PictureHolder viewHolder, @NonNull PictureViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
