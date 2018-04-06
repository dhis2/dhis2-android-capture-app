package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormEditTextCustomBinding;

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

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
    }

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
//        FormEditTextCustomBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_edit_text_custom, viewGroup, false);
        ViewDataBinding binding;
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view, viewGroup, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view_accent, viewGroup, true);

        return new EditTextCustomHolder(viewGroup, binding, processor, isBgTransparent);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextModel viewModel) {
        viewHolder.update(viewModel);
    }


}
