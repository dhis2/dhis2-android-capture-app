package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextModel> {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private final String renderType;

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
    }

    public EditTextRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                inflater,
                isBgTransparent ? R.layout.custom_text_view : R.layout.custom_text_view_accent,
                viewGroup,
                false
        );
        binding.setVariable(BR.renderType, renderType);
        binding.executePendingBindings();
        return new EditTextCustomHolder(viewGroup, binding
                , processor, isBgTransparent);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextModel viewModel) {
        viewHolder.update(viewModel);
    }

}
