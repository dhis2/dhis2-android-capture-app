package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextViewModel> {

    ViewDataBinding binding;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public EditTextRow(FlowableProcessor<RowAction> processor) {
        this.processor = processor;
    }

    public EditTextCustomHolder onCreate(ViewDataBinding binding, ViewGroup viewGroup) {
        return new EditTextCustomHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextViewModel viewModel) {

    }


}
