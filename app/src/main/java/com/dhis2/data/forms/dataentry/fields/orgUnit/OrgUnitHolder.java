package com.dhis2.data.forms.dataentry.fields.orgUnit;

import android.support.v4.app.FragmentManager;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormButtonBinding;
import com.dhis2.utils.CustomViews.OrgUnitDialog;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {
    private final FlowableProcessor<RowAction> processor;
    private List<OrganisationUnitModel> orgUnits;
    FormButtonBinding binding;
    OrgUnitDialog orgUnitDialog;
    private OrgUnitViewModel viewModel;


    public OrgUnitHolder(FragmentManager fm, FormButtonBinding binding, FlowableProcessor<RowAction> processor, Observable<List<OrganisationUnitModel>> orgUnits) {
        super(binding);
        this.binding = binding;
        this.processor = processor;

        binding.formButton.setOnClickListener(view ->
                orgUnitDialog.show(fm, binding.getLabel()));

        orgUnitDialog = OrgUnitDialog.newInstace(false)
                .setPossitiveListener(data -> {
                    processor.onNext(RowAction.create(viewModel.uid(), orgUnitDialog.getSelectedOrgUnit()));
                    binding.formButton.setText(viewModel.label() + ": " + orgUnitDialog.getSelectedOrgUnitName());
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(data -> orgUnitDialog.dismiss());

        orgUnits
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        orgUnitViewModels ->
                        {
                            this.orgUnits = orgUnitViewModels;
                            orgUnitDialog.setOrgUnits(orgUnitViewModels);
                        },
                        Timber::d
                );
    }

    public void update(OrgUnitViewModel viewModel) {
        this.viewModel = viewModel;
        binding.setLabel(viewModel.label());
        orgUnitDialog.setTitle(viewModel.label());
        if (viewModel.value() != null) {
            binding.formButton.setText(viewModel.label() + ": " + getOrgUnitName(viewModel.value()));
        }

    }

    private String getOrgUnitName(String value) {
        String orgUnitName = "Unkown";
        for (OrganisationUnitModel orgUnit : orgUnits) {
            if (orgUnit.uid().equals(value))
                orgUnitName = orgUnit.displayName();
        }
        return orgUnitName;
    }
}
