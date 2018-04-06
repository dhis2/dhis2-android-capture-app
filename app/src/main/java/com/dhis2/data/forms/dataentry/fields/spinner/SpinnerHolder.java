package com.dhis2.data.forms.dataentry.fields.spinner;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.dhis2.data.forms.dataentry.OptionAdapter;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormSpinnerBinding;

import org.hisp.dhis.android.core.option.OptionModel;

import io.reactivex.processors.FlowableProcessor;

/**
 * Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends RecyclerView.ViewHolder {

    private FormSpinnerBinding binding;
    private SpinnerViewModel model;


    SpinnerHolder(FormSpinnerBinding binding, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent) {
        super(binding.getRoot());
        this.binding = binding;
        this.binding.setIsBgTransparent(isBackgroundTransparent);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position > 0) {
                    processor.onNext(
                            RowAction.create(model.uid(), ((OptionModel) adapterView.getItemAtPosition(position - 1)).displayName())
                    );
                }
                if (view != null)
                    ((TextView) view).setTextColor(isBackgroundTransparent ? Color.BLACK : Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.executePendingBindings();

    }

    public void update(SpinnerViewModel viewModel) {

        if (model == null || binding.spinner.getAdapter() == null) {
            binding.setLabel(viewModel.label());
            binding.setOptionSet(viewModel.optionSet());
            binding.executePendingBindings();
            if (viewModel.value() != null && binding.spinner.getAdapter() != null) {
                for (int i = 0; i < ((OptionAdapter) binding.spinner.getAdapter()).getOptionCount(); i++) {
                    if (((OptionModel) binding.spinner.getAdapter().getItem(i)).displayName().equals(viewModel.value()))
                        binding.spinner.setSelection(i, false);
                }
            }
        }
        this.model = viewModel;

    }
}
