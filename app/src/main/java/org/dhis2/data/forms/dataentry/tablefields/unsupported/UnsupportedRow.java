package org.dhis2.data.forms.dataentry.tablefields.unsupported;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormUnsupportedCellBinding;

import io.reactivex.processors.FlowableProcessor;

public class UnsupportedRow implements Row<UnsupportedHolder, UnsupportedViewModel> {
    FormUnsupportedCellBinding binding;
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public UnsupportedRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public UnsupportedHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_unsupported_cell, parent, false);
        return new UnsupportedHolder(binding);
    }

    @Override
    public void onBind(@NonNull UnsupportedHolder viewHolder, @NonNull UnsupportedViewModel viewModel, String value) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull UnsupportedHolder viewHolder) {
        viewHolder.dispose();
    }
}
