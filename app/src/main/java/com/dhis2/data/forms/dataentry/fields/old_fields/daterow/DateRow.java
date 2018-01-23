package com.dhis2.data.forms.dataentry.fields.old_fields.daterow;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

public final class DateRow implements Row<DateViewHolder, DateViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FragmentManager fragmentManager;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public DateRow(@NonNull LayoutInflater inflater, @NonNull FragmentManager manager,
                   @NonNull FlowableProcessor<RowAction> processor) {
        this.inflater = inflater;
        this.processor = processor;
        this.fragmentManager = manager;
    }

    @NonNull
    @Override
    public DateViewHolder onCreate(@NonNull ViewGroup parent) {
        return new DateViewHolder(fragmentManager, inflater.inflate(
                R.layout.recyclerview_row_date, parent, false), parent, processor);
    }

    @Override
    public void onBind(@NonNull DateViewHolder viewHolder, @NonNull DateViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
