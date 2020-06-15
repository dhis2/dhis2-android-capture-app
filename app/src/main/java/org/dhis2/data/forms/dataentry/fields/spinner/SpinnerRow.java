package org.dhis2.data.forms.dataentry.fields.spinner;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FormOptionSetBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class SpinnerRow implements Row<SpinnerHolder, SpinnerViewModel> {


    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBackgroundTransparent;
    private final String renderType;
    private final LayoutInflater inflater;
    private final MutableLiveData<String> currentSelection;
    private boolean isSearchMode = false;

    public SpinnerRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isBackgroundTransparent) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = null;
        this.inflater = layoutInflater;
        this.isSearchMode = true;
        this.currentSelection = null;
    }

    public SpinnerRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor,
                      FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, boolean isBackgroundTransparent, String renderType,
                      MutableLiveData<String> currentSelection) {
        this.processor = processor;
        this.isBackgroundTransparent = isBackgroundTransparent;
        this.renderType = renderType;
        this.inflater = layoutInflater;
        this.currentSelection = currentSelection;
    }

    @NonNull
    @Override
    public SpinnerHolder onCreate(@NonNull ViewGroup parent) {
        FormOptionSetBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_option_set, parent, false);
        binding.optionSetView.setLayoutData(isBackgroundTransparent, renderType);
        return new SpinnerHolder(binding, processor, isSearchMode,currentSelection);
    }

    @Override
    public void onBind(@NonNull SpinnerHolder viewHolder, @NonNull SpinnerViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
