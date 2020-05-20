package org.dhis2.data.forms.dataentry.tablefields.coordinate;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.coodinates.CoordinatesView;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.customviews.TableFieldDialog;
import org.hisp.dhis.android.core.common.FeatureType;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    CustomCellViewBinding binding;
    TextView textView;
    CoordinateViewModel model;
    Context context;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomCellViewBinding binding, FlowableProcessor<RowAction> processor, Context context) {
        super(binding);
        this.binding = binding;
        this.context = context;
        this.processor = processor;
        textView = binding.inputEditText;

    }

    void update(CoordinateViewModel coordinateViewModel, boolean accessDataWrite) {

        this.model = coordinateViewModel;

        if (!isEmpty(coordinateViewModel.value()))
            textView.setText(coordinateViewModel.value());
        else
            textView.setText(null);

        if (!(accessDataWrite && coordinateViewModel.editable())) {
            textView.setEnabled(false);
        } else
            textView.setEnabled(true);

        if (coordinateViewModel.mandatory())
            binding.icMandatory.setVisibility(View.VISIBLE);
        else
            binding.icMandatory.setVisibility(View.INVISIBLE);

        binding.executePendingBindings();
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && textView.isEnabled()) {
            showEditDialog();
        }
    }

    private void showEditDialog() {

        CoordinatesView coordinatesView = new CoordinatesView(context);
        coordinatesView.setIsBgTransparent(true);
        coordinatesView.setFeatureType(FeatureType.POINT);

        if (model.value() != null && !model.value().isEmpty()) {
            coordinatesView.setInitialValue(model.value());
        }

        new TableFieldDialog(
                context,
                model.label(),
                model.description(),
                coordinatesView,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        processor.onNext(RowAction.create(model.uid(), coordinatesView.currentCoordinates(),
                                model.dataElement(), model.categoryOptionCombo(), model.catCombo(), model.row(), model.column()));
                    }

                    @Override
                    public void onNegative() {
                    }
                },
                null
        ).show();
    }

    @Override
    public void dispose() {
    }
}