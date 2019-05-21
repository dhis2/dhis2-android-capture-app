package org.dhis2.data.forms.dataentry.fields.orgUnit;

import androidx.fragment.app.FragmentManager;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormOrgUnitBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {
    private final FormOrgUnitBinding formOrgUnitBinding;
    private final Observable<List<OrganisationUnitModel>> orgUnitsObservable;
    private List<OrganisationUnitModel> orgUnits;
    private CompositeDisposable compositeDisposable;
    private OrgUnitViewModel model;

    OrgUnitHolder(FragmentManager fm, FormOrgUnitBinding binding, FlowableProcessor<RowAction> processor, Observable<List<OrganisationUnitModel>> orgUnits, Observable<List<OrganisationUnitLevel>> levels) {
        super(binding);
        this.formOrgUnitBinding = binding;
        compositeDisposable = new CompositeDisposable();

        this.orgUnitsObservable = orgUnits;

        binding.orgUnitView.setListener(orgUnitUid -> {
            processor.onNext(RowAction.create(model.uid(), orgUnitUid));
        });

        getOrgUnits();
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    public void update(OrgUnitViewModel viewModel) {
        this.model = viewModel;
        formOrgUnitBinding.orgUnitView.setObjectStyle(viewModel.objectStyle());
        formOrgUnitBinding.orgUnitView.setLabel(viewModel.label(), viewModel.mandatory());
        descriptionText = viewModel.description();
        formOrgUnitBinding.orgUnitView.setDescription(descriptionText);
        formOrgUnitBinding.orgUnitView.setWarning(viewModel.warning(), viewModel.error());
        formOrgUnitBinding.orgUnitView.setValue(viewModel.value(), getOrgUnitName(viewModel.value()));
        formOrgUnitBinding.orgUnitView.updateEditable(viewModel.editable());


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
                            if (model.value() != null) {
                                update(model);
                            }
                        },
                        Timber::d
                )
        );
    }
}
