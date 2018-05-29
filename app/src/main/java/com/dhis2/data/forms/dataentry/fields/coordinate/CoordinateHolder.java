package com.dhis2.data.forms.dataentry.fields.coordinate;


import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.CustomFormCoordinateBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.utils.CustomViews.CoordinatesView;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public class CoordinateHolder extends FormViewHolder {

    private SearchTEContractsModule.Presenter presenter;
    private TrackedEntityAttributeModel bindableObject;

    @NonNull
    private
    BehaviorProcessor<CoordinateViewModel> model;

    @SuppressLint("CheckResult")
    CoordinateHolder(CustomFormCoordinateBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        binding.formCoordinates.setCurrentLocationListener((latitude, longitude) ->
                processor.onNext(
                        RowAction.create(model.getValue().uid(),
                                String.format(Locale.getDefault(),
                                        "[%.5f,%.5f]", latitude, longitude))
                ));
        binding.formCoordinates.setMapListener(
                (CoordinatesView.OnMapPositionClick) binding.formCoordinates.getContext()
        );
        CompositeDisposable disposable = new CompositeDisposable();
        model = BehaviorProcessor.create();

        disposable.add(model.subscribe(coordinateViewModel -> {
            binding.formCoordinates.setLabel(coordinateViewModel.label());
            if (!isEmpty(coordinateViewModel.value()))
                binding.formCoordinates.setInitialValue(coordinateViewModel.value());
            binding.executePendingBindings();
        },
                Timber::d));
    }

    void update(CoordinateViewModel viewModel) {
        model.onNext(viewModel);
    }

}