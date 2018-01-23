package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

public final class EditTextRow implements Row<EditTextViewHolder, EditTextModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public EditTextRow(@NonNull LayoutInflater inflater,
                       @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = inflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public EditTextViewHolder onCreate(@NonNull ViewGroup parent) {
        return new EditTextViewHolder(parent, inflater.inflate(
                R.layout.recyclerview_row_edittext, parent, false), processor);
    }

    @Override
    public void onBind(@NonNull EditTextViewHolder viewHolder, @NonNull EditTextModel viewModel) {
        viewHolder.update(viewModel);
    }
}