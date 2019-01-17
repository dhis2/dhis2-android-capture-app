package org.dhis2.data.forms.dataentry.tablefields.coordinate;


import android.annotation.SuppressLint;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;
import org.dhis2.utils.CustomViews.CoordinatesView;

import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    CustomFormCoordinateBinding binding;
    CoordinateViewModel model;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.binding = binding;
        binding.formCoordinates.setCurrentLocationListener((latitude, longitude) ->
                processor.onNext(
                        RowAction.create(model.uid(),
                                String.format(Locale.US,
                                        "[%.5f,%.5f]", latitude, longitude))
                ));
        binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) binding.formCoordinates.getContext()
        );

    }

    void update(CoordinateViewModel coordinateViewModel) {
        model = coordinateViewModel;

        descriptionText = coordinateViewModel.description();
        label = new StringBuilder(coordinateViewModel.label());
        if (coordinateViewModel.mandatory())
            label.append("*");
        binding.formCoordinates.setLabel(label.toString());
        binding.formCoordinates.setDescription(descriptionText);

        if (!isEmpty(coordinateViewModel.value()))
            binding.formCoordinates.setInitialValue(coordinateViewModel.value());

        if (coordinateViewModel.warning() != null)
            binding.formCoordinates.setWargingOrError(coordinateViewModel.warning());
        else if (coordinateViewModel.error() != null)
            binding.formCoordinates.setWargingOrError(coordinateViewModel.error());
        else
            binding.formCoordinates.setWargingOrError(null);

        binding.formCoordinates.setEditable(coordinateViewModel.editable());

        binding.executePendingBindings();
    }

    @Override
    public void dispose() {
    }
}