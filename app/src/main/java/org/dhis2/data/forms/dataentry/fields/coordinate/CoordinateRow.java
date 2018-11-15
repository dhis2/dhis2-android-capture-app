package org.dhis2.data.forms.dataentry.fields.coordinate;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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

    public CoordinateRow(@NonNull LayoutInflater layoutInflater,
                         @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
    }

    public CoordinateRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                         @NonNull FlowableProcessor<Integer> currentPosition,boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType= renderType;
    }

    @NonNull
    @Override
    public CoordinateHolder onCreate(@NonNull ViewGroup parent) {
        CustomFormCoordinateBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_form_coordinate, parent, false);
        binding.formCoordinates.setIsBgTransparent(isBgTransparent);
        return new CoordinateHolder(binding, processor);
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
