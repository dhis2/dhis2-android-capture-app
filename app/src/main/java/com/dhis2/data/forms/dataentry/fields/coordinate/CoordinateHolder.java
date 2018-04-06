package com.dhis2.data.forms.dataentry.fields.coordinate;


import android.support.annotation.NonNull;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.CustomFormCoordinateBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

public class CoordinateHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    @NonNull
    BehaviorProcessor<CoordinateViewModel> model;

    public CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);

        model = BehaviorProcessor.create();

        model.subscribe(coordinateViewModel -> {

        });
    }

   /* @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }*/

    void update(CoordinateViewModel viewModel) {

        model.onNext(viewModel);
    }
}