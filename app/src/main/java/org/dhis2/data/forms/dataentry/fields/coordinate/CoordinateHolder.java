package org.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;
import org.dhis2.usescases.coodinates.CoordinatesView;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private final CustomFormCoordinateBinding binding;
    private CoordinateViewModel coordinateViewModel;

    @SuppressLint("CheckResult")
    public CoordinateHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        this.binding = (CustomFormCoordinateBinding) binding;
        this.currentUid = currentSelection;

        this.binding.formCoordinates.setCurrentLocationListener(geometry -> {
                    closeKeyboard(this.binding.formCoordinates);
                    processor.onNext(
                            RowAction.create(coordinateViewModel.uid(),
                                    geometry == null ? null : geometry.coordinates(),
                                    getAdapterPosition(),
                                    coordinateViewModel.featureType().name()));
                    clearBackground(isSearchMode);
                }
        );
        this.binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) this.binding.formCoordinates.getContext()
        );

        this.binding.formCoordinates.setActivationListener(() -> setSelectedBackground(isSearchMode));

    }

    @Override
    public void update(FieldViewModel viewModel) {
        this.coordinateViewModel = (CoordinateViewModel) viewModel;
        binding.formCoordinates.setFeatureType(coordinateViewModel.featureType());
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

        setFormFieldBackground();
    }
}