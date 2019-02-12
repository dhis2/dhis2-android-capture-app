package org.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;
import org.dhis2.utils.custom_views.CoordinatesView;

import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;
    private CustomFormCoordinateBinding customFormCoordinateBinding;
    private CoordinateViewModel model;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.processor = processor;
        this.customFormCoordinateBinding = binding;
        binding.formCoordinates.setCurrentLocationListener((latitude, longitude) -> processor.onNext(
                RowAction.create(model.uid(),
                        String.format(Locale.US,
                                "[%.5f,%.5f]", latitude, longitude)))
        );
        binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) binding.formCoordinates.getContext()
        );

    }

    void update(CoordinateViewModel coordinateViewModel) {
        customFormCoordinateBinding.formCoordinates.setProcessor(coordinateViewModel.uid(), processor);

        model = coordinateViewModel;

        descriptionText = coordinateViewModel.description();
        label = new StringBuilder(coordinateViewModel.label());
        if (coordinateViewModel.mandatory())
            label.append("*");
        customFormCoordinateBinding.formCoordinates.setLabel(label.toString());
        customFormCoordinateBinding.formCoordinates.setDescription(descriptionText);

        if (!isEmpty(coordinateViewModel.value()))
            customFormCoordinateBinding.formCoordinates.setInitialValue(coordinateViewModel.value());

        if (coordinateViewModel.warning() != null)
            customFormCoordinateBinding.formCoordinates.setWargingOrError(coordinateViewModel.warning());
        else if (coordinateViewModel.error() != null)
            customFormCoordinateBinding.formCoordinates.setWargingOrError(coordinateViewModel.error());
        else
            customFormCoordinateBinding.formCoordinates.setWargingOrError(null);

        customFormCoordinateBinding.formCoordinates.setEditable(coordinateViewModel.editable());

        customFormCoordinateBinding.executePendingBindings();
    }

    @Override
    public void dispose() {
        // do nothing
    }
}