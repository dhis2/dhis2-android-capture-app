package org.dhis2.data.forms.dataentry.tablefields.coordinate;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
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
    private boolean accessDataWrite;

    public CoordinateRow(@NonNull LayoutInflater layoutInflater,
                         @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
    }

    public CoordinateRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent, String renderType, boolean accessDataWrite) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType= renderType;
        this.accessDataWrite = accessDataWrite;
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
    public void onBind(@NonNull CoordinateHolder viewHolder, @NonNull CoordinateViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite);
    }

    @Override
    public void deAttach(@NonNull CoordinateHolder viewHolder) {
        viewHolder.dispose();
    }
}
