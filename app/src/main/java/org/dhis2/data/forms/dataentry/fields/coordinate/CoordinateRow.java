package org.dhis2.data.forms.dataentry.fields.coordinate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class CoordinateRow implements Row<CoordinateHolder, CoordinateViewModel> {

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    @NonNull
    private final LayoutInflater inflater;
    private final boolean isBgTransparent;
    private final String renderType;
    private final MutableLiveData<String> currentSelection;
    private boolean isSearchMode = false;

    public CoordinateRow(@NonNull LayoutInflater layoutInflater,
                         @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        this.isSearchMode = true;
        this.currentSelection = null;
    }

    public CoordinateRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor,
                         boolean isBgTransparent, String renderType,
                         MutableLiveData<String> currentSelection) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
        this.currentSelection = currentSelection;
    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormCoordinateBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_form_coordinate, parent, false);
        binding.formCoordinates.setIsBgTransparent(isBgTransparent);
        return new CoordinateHolder(binding, processor, isSearchMode, currentSelection);
    }

    @Override
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull CoordinateHolder viewHolder) {
        viewHolder.dispose();
    }
}
