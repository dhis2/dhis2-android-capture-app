package org.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;
import android.graphics.Color;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.CustomFormCoordinateBinding;
import org.dhis2.utils.custom_views.CoordinatesView;

import java.util.Locale;

import androidx.appcompat.content.res.AppCompatResources;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;
    private CustomFormCoordinateBinding binding;
    private CoordinateViewModel model;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.processor = processor;
        this.binding = binding;
        binding.formCoordinates.setCurrentLocationListener((latitude, longitude) -> {
                    closeKeyboard(binding.formCoordinates);
                    processor.onNext(
                            RowAction.create(model.uid(),
                                    String.format(Locale.US, "[%.5f,%.5f]", latitude, longitude),
                                    getAdapterPosition()));
                    if (!isSearchMode)
                        itemView.setBackgroundColor(Color.WHITE);
                }
        );
        binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) binding.formCoordinates.getContext()
        );

    }

    void update(CoordinateViewModel coordinateViewModel) {
        binding.formCoordinates.setProcessor(coordinateViewModel.uid(), processor);

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
            binding.formCoordinates.setWarning(coordinateViewModel.warning());
        else if (coordinateViewModel.error() != null)
            binding.formCoordinates.setError(coordinateViewModel.error());
        else
            binding.formCoordinates.setError(null);

        binding.formCoordinates.setEditable(coordinateViewModel.editable());

        binding.executePendingBindings();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        binding.formCoordinates.performOnFocusAction();
    }
}