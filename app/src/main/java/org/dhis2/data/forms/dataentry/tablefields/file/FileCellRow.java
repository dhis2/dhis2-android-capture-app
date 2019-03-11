package org.dhis2.data.forms.dataentry.tablefields.file;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormButtonBinding;
import org.dhis2.databinding.FormButtonCellBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class FileCellRow implements Row<FileHolder, FileViewModel> {
    FormButtonCellBinding binding;
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;


    public FileCellRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor) {
        this.inflater = layoutInflater;
        this.processor = processor;
    }

    @NonNull
    @Override
    public FileHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_button_cell, parent, false);
        binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimary));
        return new FileHolder(binding);
    }

    @Override
    public void onBind(@NonNull FileHolder viewHolder, @NonNull FileViewModel viewModel, String value) {
        binding.setLabel(viewModel.label());
    }

    @Override
    public void deAttach(@NonNull FileHolder viewHolder) {

    }
}
