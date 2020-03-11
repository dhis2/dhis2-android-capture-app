package org.dhis2.data.forms.dataentry.fields.option_set;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormOptionSetSelectorBinding;

import io.reactivex.processors.FlowableProcessor;

public class OptionSetRow implements Row<OptionSetHolder, OptionSetViewModel> {

    private final boolean isBgTransparent;
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;
    private final MutableLiveData<String> currentSelection;
    private final String renderType;
    private boolean isSearchMode = false;

    public OptionSetRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                        boolean isBgTransparent, String renderType, MutableLiveData<String> currentFocusUid) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.currentSelection = currentFocusUid;
        this.renderType = renderType;

    }

    @NonNull
    @Override
    public OptionSetHolder onCreate(@NonNull ViewGroup parent) {
        FormOptionSetSelectorBinding binding = FormOptionSetSelectorBinding.inflate(inflater, parent, false);
        binding.optionSetSelectionView.setLayoutData(isBgTransparent, renderType);
        return new OptionSetHolder(binding, processor, isSearchMode, currentSelection);
    }

    @Override
    public void onBind(@NonNull OptionSetHolder viewHolder, @NonNull OptionSetViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
