package com.dhis2.data.forms.dataentry.fields.orgUnit;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.utils.CustomViews.OrgUnitDialog;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {
    private final TextInputEditText editText;
    private final TextInputLayout inputLayout;
    private final Observable<List<OrganisationUnitModel>> orgUnitsObservable;
    private List<OrganisationUnitModel> orgUnits;
    private OrgUnitDialog orgUnitDialog;
    private CompositeDisposable compositeDisposable;
    @NonNull
    private BehaviorProcessor<OrgUnitViewModel> model;

    OrgUnitHolder(FragmentManager fm, ViewDataBinding binding, FlowableProcessor<RowAction> processor, Observable<List<OrganisationUnitModel>> orgUnits) {
        super(binding);
        compositeDisposable = new CompositeDisposable();
        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.orgUnitsObservable = orgUnits;

        this.editText.setOnClickListener(view -> {
            orgUnitDialog = new OrgUnitDialog()
                    .setTitle(model.getValue().label())
                    .setMultiSelection(false)
                    .setOrgUnits(this.orgUnits)
                    .setPossitiveListener(data -> {
                        processor.onNext(RowAction.create(model.getValue().uid(), orgUnitDialog.getSelectedOrgUnit()));
                        this.editText.setText(orgUnitDialog.getSelectedOrgUnitName());
                        orgUnitDialog.dismiss();
                    })
                    .setNegativeListener(data -> orgUnitDialog.dismiss());
            orgUnitDialog.show(fm, model.getValue().label());
        });


        model = BehaviorProcessor.create();

        compositeDisposable.add(
                model.subscribe(viewModel -> {
                            StringBuilder label = new StringBuilder(viewModel.label());
                            if (viewModel.mandatory())
                                label.append("*");
                            this.inputLayout.setHint(label.toString());

                            if (viewModel.warning() != null)
                                editText.setError(viewModel.warning());
                            else if (viewModel.error() != null)
                                editText.setError(viewModel.error());
                            else
                                editText.setError(null);

                            if (viewModel.value() != null && !viewModel.value().equals(viewModel.value())) {
                                getOrgUnits();
                            }
                            editText.setEnabled(viewModel.editable());
                        },
                        Timber::d)
        );

        getOrgUnits();
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    public void update(OrgUnitViewModel viewModel) {
        model.onNext(viewModel);
    }

    private String getOrgUnitName(String value) {
        String orgUnitName = null;
        if (orgUnits != null) {
            for (OrganisationUnitModel orgUnit : orgUnits) {
                if (orgUnit.uid().equals(value))
                    orgUnitName = orgUnit.displayName();
            }
        }
        return orgUnitName;
    }

    private void getOrgUnits() {
        compositeDisposable.add(orgUnitsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        orgUnitViewModels ->
                        {
                            this.orgUnits = orgUnitViewModels;
                            if (model.getValue().value() != null) {
                                this.inputLayout.setHintAnimationEnabled(false);
                                this.editText.setText(getOrgUnitName(model.getValue().value()));
                                this.inputLayout.setHintAnimationEnabled(true);
                            }
                        },
                        Timber::d
                )
        );
    }
}
