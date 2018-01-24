package com.dhis2.data.forms.dataentry.fields.old_fields.radiobutton;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

public final class RadioButtonRow implements Row<RadioButtonViewHolder, RadioButtonViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public RadioButtonRow(@NonNull LayoutInflater inflater,
            @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = inflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public RadioButtonViewHolder onCreate(@NonNull ViewGroup parent) {
        return new RadioButtonViewHolder(parent, inflater.inflate(
                R.layout.recyclerview_row_radiobutton, parent, false), processor);
    }

    @Override
    public void onBind(@NonNull RadioButtonViewHolder viewHolder, @NonNull RadioButtonViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}