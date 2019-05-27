package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.graphics.Color;

import androidx.core.content.ContextCompat;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormOrgUnitBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitHolder extends FormViewHolder {

    private final FormOrgUnitBinding formOrgUnitBinding;
    private CompositeDisposable compositeDisposable;
    private OrgUnitViewModel model;

    OrgUnitHolder(FormOrgUnitBinding binding, FlowableProcessor<RowAction> processor,
                  boolean isSearchMode) {
        super(binding);
        this.formOrgUnitBinding = binding;
        compositeDisposable = new CompositeDisposable();
        formOrgUnitBinding.orgUnitView.setListener(orgUnitUid -> {
            processor.onNext(RowAction.create(model.uid(), orgUnitUid, getAdapterPosition()));
            if (!isSearchMode)
                itemView.setBackgroundColor(Color.WHITE);
        });
    }

    @Override
    public void dispose() {
        compositeDisposable.clear();
    }

    @Override
    public void performAction() {
        itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        formOrgUnitBinding.orgUnitView.performOnFocusAction();
    }

    public void update(OrgUnitViewModel viewModel) {
        this.model = viewModel;
        String uidValueName = viewModel.value();
        String ouUid = null;
        String ouName = null;
        if (!isEmpty(uidValueName)) {
            ouUid = uidValueName.split("_ou_")[0];
            ouName = uidValueName.split("_ou_")[1];
        }

        formOrgUnitBinding.orgUnitView.setObjectStyle(viewModel.objectStyle());
        formOrgUnitBinding.orgUnitView.setLabel(viewModel.label(), viewModel.mandatory());
        descriptionText = viewModel.description();
        formOrgUnitBinding.orgUnitView.setDescription(descriptionText);
        formOrgUnitBinding.orgUnitView.setWarning(viewModel.warning(), viewModel.error());
        formOrgUnitBinding.orgUnitView.setValue(ouUid, ouName);
        formOrgUnitBinding.orgUnitView.getEditText().setText(ouName);
        formOrgUnitBinding.orgUnitView.updateEditable(viewModel.editable());
        label = new StringBuilder().append(viewModel.label());
    }
}
