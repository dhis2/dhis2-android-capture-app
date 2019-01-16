package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.utils.custom_views.TextInputAutoCompleteTextView;
import org.dhis2.utils.custom_views.orgUnitCascade.OrgUnitCascadeDialog;
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
    private final TextInputAutoCompleteTextView editText;
    private final TextInputLayout inputLayout;
    private final Observable<List<OrganisationUnitModel>> orgUnitsObservable;
    private List<OrganisationUnitModel> orgUnits;
    private OrgUnitCascadeDialog orgUnitDialog;
    private CompositeDisposable compositeDisposable;
    private OrgUnitViewModel model;
    private String selectedOrgUnit;

    OrgUnitHolder(FragmentManager fm, ViewDataBinding binding, FlowableProcessor<RowAction> processor, Observable<List<OrganisationUnitModel>> orgUnits) {
        super(binding);
        compositeDisposable = new CompositeDisposable();
        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.orgUnitsObservable = orgUnits;

        this.editText.setOnClickListener(view -> {
            editText.setEnabled(false);
            orgUnitDialog = new OrgUnitCascadeDialog()
                    .setTitle(model.label())
                    .setOrgUnits(this.orgUnits)
                    .setSelectedOrgUnit(model.value())
                    .setCallbacks(new OrgUnitCascadeDialog.CascadeOrgUnitCallbacks() {
                        @Override
                        public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
                            selectedOrgUnit = selectedOrgUnitUid;
                            processor.onNext(RowAction.create(model.uid(), selectedOrgUnitUid));
                            editText.setText(selectedOrgUnitName);
                            orgUnitDialog.dismiss();
                            editText.setEnabled(true);
                        }

                        @Override
                        public void onDialogCancelled() {
                            editText.setEnabled(true);
                        }
                    });

            if (!orgUnitDialog.isAdded())
                orgUnitDialog.show(fm, model.label());
        });


        getOrgUnits();
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    public void update(OrgUnitViewModel viewModel) {

        descriptionText = viewModel.description();
        label = new StringBuilder(viewModel.label());
        if (viewModel.mandatory())
            label.append("*");
        this.inputLayout.setHint(label.toString());

        if (label.length() > 16 || viewModel.description() != null)
            description.setVisibility(View.VISIBLE);
        else
            description.setVisibility(View.GONE);

        if (viewModel.warning() != null)
            editText.setError(viewModel.warning());
        else if (viewModel.error() != null)
            editText.setError(viewModel.error());
        else
            editText.setError(null);

        if (viewModel.value() != null) {
            editText.post(() -> editText.setText(getOrgUnitName(viewModel.value())));
        }
        editText.setEnabled(viewModel.editable());

        this.model = viewModel;

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
                                this.inputLayout.setHintAnimationEnabled(false);
                                this.editText.setText(getOrgUnitName(model.value()));
                                this.inputLayout.setHintAnimationEnabled(true);
                            }
                        },
                        Timber::d
                )
        );
    }
}
