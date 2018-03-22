package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormEditTextBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextViewModel> {


    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public EditTextRow(FlowableProcessor<RowAction> processor) {
        this.processor = processor;
    }

    public EditTextCustomHolder onCreate(ViewGroup viewGroup) {
        FormEditTextBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.form_edit_text, viewGroup, false);
        return new EditTextCustomHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextViewModel viewModel) {

    }


}
