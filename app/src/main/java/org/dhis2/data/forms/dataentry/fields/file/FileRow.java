package org.dhis2.data.forms.dataentry.fields.file;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormButtonBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class FileRow implements Row<FileHolder, FileViewModel> {
    private final String renderType;
    FormButtonBinding binding;
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private boolean isSearchMode = false;


    public FileRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        isSearchMode = true;
    }

    public FileRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                   @NonNull FlowableProcessor<Integer> currentPosition,boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public FileHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_button, parent, false);
        if (isBgTransparent)
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimary));
        else
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorAccent));

        return new FileHolder(binding);
    }

    @Override
    public void onBind(@NonNull FileHolder viewHolder, @NonNull FileViewModel viewModel) {
        binding.setLabel(viewModel.label());
    }

    @Override
    public void deAttach(@NonNull FileHolder viewHolder) {

    }
}
