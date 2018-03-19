package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        return new EditTextCustomHolder(DataBindingUtil.inflate(inflater,
                R.layout.form_edit_text_custom, viewGroup, false), processor);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextViewModel viewModel) {
        viewHolder.update(viewModel);
    }


}
