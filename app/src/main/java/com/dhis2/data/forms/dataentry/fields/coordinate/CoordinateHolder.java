package com.dhis2.data.forms.dataentry.fields.coordinate;


import android.support.annotation.NonNull;
import android.view.View;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormCoordinatesBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

public class CoordinateHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    @NonNull
    BehaviorProcessor<CoordinateViewModel> model;

    public CoordinateHolder(FormCoordinatesBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);

        model = BehaviorProcessor.create();

        model.subscribe(coordinateViewModel -> {
            if (coordinateViewModel.value() != null) {
                binding.lat.setText(coordinateViewModel.value().split(",")[0]);
                binding.lon.setText(coordinateViewModel.value().split(",")[1]);
            }
        });

        binding.location1.setOnClickListener(v -> {
            //TODO: get location from FusedLocationProvider
        });

        binding.location2.setOnClickListener(v -> {
            //TODO: open map to select Location
        });
    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }

    void update(CoordinateViewModel viewModel) {

        model.onNext(viewModel);
    }
}