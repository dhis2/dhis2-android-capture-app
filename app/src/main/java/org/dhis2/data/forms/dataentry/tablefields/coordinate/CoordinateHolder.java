package org.dhis2.data.forms.dataentry.tablefields.coordinate;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.custom_views.CoordinatesView;
import org.hisp.dhis.android.core.common.FeatureType;

import java.util.Locale;

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

        if(!(accessDataWrite && coordinateViewModel.editable())) {
            textView.setEnabled(false);
        }else
            textView.setEnabled(true);

        if(coordinateViewModel.mandatory())
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
        View view = LayoutInflater.from(context).inflate(R.layout.custom_form_coordinate, null);
        CoordinatesView coordinatesView = view.findViewById(R.id.formCoordinates);
        coordinatesView.setIsBgTransparent(true);

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    if (coordinatesView.validateInputtedData()){
                        processor.onNext(RowAction.create(model.uid(), String.format(Locale.US, "[%.5f,%.5f]", coordinatesView.getLatitude(), coordinatesView.getLongitude()), model.dataElement(), model.categoryOptionCombo(), model.catCombo(), model.row(), model.column()));
                    }
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> processor.onNext(
                        RowAction.create(model.uid(), null, model.dataElement(), model.categoryOptionCombo(), model.catCombo(), model.row(), model.column())))
                .create();

        if(model.value() != null && !model.value().isEmpty()) {
            coordinatesView.setFeatureType(FeatureType.POINT);
            coordinatesView.setInitialValue(model.value());
        }

        coordinatesView.setLabel(model.label());

        coordinatesView.setCurrentLocationListener((geometry) -> {
            processor.onNext(RowAction.create(model.uid(), geometry.coordinates(), model.dataElement(), model.categoryOptionCombo(), model.catCombo(), model.row(), model.column()));
            alertDialog.dismiss();
        });

        coordinatesView.setMapListener(
                (CoordinatesView.OnMapPositionClick) coordinatesView.getContext()
        );
        alertDialog.setView(view);

        alertDialog.show();
    }

    @Override
    public void dispose() {
    }
}