package com.dhis2.data.forms.dataentry.fields.old_fields.checkbox;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

public class CheckBoxRow implements Row<CheckBoxViewHolder, CheckBoxViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public CheckBoxRow(@NonNull LayoutInflater inflater,
                       @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = inflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public CheckBoxViewHolder onCreate(@NonNull ViewGroup parent) {
        return new CheckBoxViewHolder(parent, inflater.inflate(
                R.layout.recyclerview_row_checkbox, parent, false), processor);
    }

    @Override
    public void onBind(@NonNull CheckBoxViewHolder viewHolder, @NonNull CheckBoxViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}