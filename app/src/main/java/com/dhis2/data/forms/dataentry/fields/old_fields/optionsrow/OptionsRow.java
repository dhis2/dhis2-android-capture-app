package com.dhis2.data.forms.dataentry.fields.old_fields.optionsrow;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.DataEntryArguments;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import io.reactivex.processors.FlowableProcessor;

public final class OptionsRow implements Row<OptionsViewHolder, OptionsViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final FragmentManager fragmentManager;

    @NonNull
    private final DataEntryArguments dataEntryArguments;

    @NonNull
    private final FlowableProcessor<RowAction> flowableProcessor;

    public OptionsRow(@NonNull LayoutInflater inflater,
            @NonNull FragmentManager fragmentManager,
            @NonNull FlowableProcessor<RowAction> flowableProcessor,
            @NonNull DataEntryArguments dataEntryArguments) {
        this.inflater = inflater;
        this.fragmentManager = fragmentManager;
        this.flowableProcessor = flowableProcessor;
        this.dataEntryArguments = dataEntryArguments;
    }

    @NonNull
    @Override
    public OptionsViewHolder onCreate(@NonNull ViewGroup parent) {
        return new OptionsViewHolder(fragmentManager, inflater.inflate(R.layout.recyclerview_row_options,
                parent, false), parent, flowableProcessor, dataEntryArguments);
    }

    @Override
    public void onBind(@NonNull OptionsViewHolder viewHolder,
            @NonNull OptionsViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
