package org.dhis2.data.forms.dataentry.fields.radiobutton;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class RadioButtonRow implements Row<RadioButtonHolder, RadioButtonViewModel> {

    private final LayoutInflater inflater;
    private final boolean isBgTransparent;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    public RadioButtonRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
    }

    @NonNull
    @Override
    public RadioButtonHolder onCreate(@NonNull ViewGroup parent) {
        FormYesNoBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.form_yes_no, parent, false);
        binding.customYesNo.setIsBgTransparent(isBgTransparent);
        return new RadioButtonHolder(binding, processor);
    }

    @Override
    public void onBind(@NonNull RadioButtonHolder viewHolder, @NonNull RadioButtonViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull RadioButtonHolder viewHolder) {
        viewHolder.dispose();
    }


}