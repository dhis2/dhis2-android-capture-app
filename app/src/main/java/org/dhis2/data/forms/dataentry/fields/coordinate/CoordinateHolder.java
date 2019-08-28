package org.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;
import org.dhis2.utils.custom_views.CoordinatesView;

import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;
    private CustomFormCoordinateBinding binding;
    private CoordinateViewModel model;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        this.processor = processor;
        this.binding = binding;
        this.currentUid = currentSelection;

        binding.formCoordinates.setCurrentLocationListener((latitude, longitude) -> {
                    closeKeyboard(binding.formCoordinates);
                    if (latitude == null || longitude == null) {
                        processor.onNext(
                                RowAction.create(model.uid(), null, getAdapterPosition()));
                    } else
                        processor.onNext(
                                RowAction.create(model.uid(),
                                        String.format(Locale.US, "[%.5f,%.5f]", longitude, latitude),
                                        getAdapterPosition()));
                    clearBackground(isSearchMode);
                }
        );
        binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) binding.formCoordinates.getContext()
        );

        binding.formCoordinates.setActivationListener(() -> setSelectedBackground(isSearchMode));

    }

    void update(CoordinateViewModel coordinateViewModel) {
        binding.formCoordinates.setProcessor(coordinateViewModel.uid(), processor);

        model = coordinateViewModel;
        fieldUid = coordinateViewModel.uid();

        descriptionText = coordinateViewModel.description();
        label = new StringBuilder(coordinateViewModel.label());
        if (coordinateViewModel.mandatory())
            label.append("*");
        binding.formCoordinates.setLabel(label.toString());
        binding.formCoordinates.setDescription(descriptionText);

        if (!isEmpty(coordinateViewModel.value()))
            binding.formCoordinates.setInitialValue(coordinateViewModel.value());
        else
            binding.formCoordinates.clearValueData();

        if (coordinateViewModel.warning() != null)
            binding.formCoordinates.setWarning(coordinateViewModel.warning());
        else if (coordinateViewModel.error() != null)
            binding.formCoordinates.setError(coordinateViewModel.error());
        else
            binding.formCoordinates.setError(null);

        binding.formCoordinates.setEditable(coordinateViewModel.editable());

        binding.executePendingBindings();
        initFieldFocus();
    }

    @Override
    public void dispose() {
    }


}