package org.dhis2.utils.filters;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.utils.filters.ou.OUFilterAdapter;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

class OrgUnitFilterHolder extends FilterHolder {

    private OrganisationUnit currentOrgUnit;

    OrgUnitFilterHolder(@NonNull ItemFilterOrgUnitBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ORG_UNIT;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_ou));
        filterTitle.setText(R.string.filters_title_org_unit);

        setUpAdapter();

    }

    private void setUpAdapter() {
        D2 d2 = ((App) itemView.getContext().getApplicationContext()).serverComponent().userManager().getD2();

        ItemFilterOrgUnitBinding localBinding = (ItemFilterOrgUnitBinding) binding;

        OUFilterAdapter ouFilterAdapter = new OUFilterAdapter();
        localBinding.filterOrgUnit.ouRecycler.setAdapter(ouFilterAdapter);

        localBinding.filterOrgUnit.orgUnitSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 3) {
                    currentOrgUnit = d2.organisationUnitModule().organisationUnits()
                            .byDisplayName().like("%" + charSequence + "%").one().blockingGet();
                    if (currentOrgUnit != null)
                        localBinding.filterOrgUnit.orgUnitHint.setText(currentOrgUnit.displayName());
                    else
                        localBinding.filterOrgUnit.orgUnitHint.setText(null);
                } else
                    localBinding.filterOrgUnit.orgUnitHint.setText(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        localBinding.filterOrgUnit.addButton.setOnClickListener(view -> {
            if (currentOrgUnit != null) {
                FilterManager.getInstance().addOrgUnit(currentOrgUnit);
                currentOrgUnit = null;
                localBinding.filterOrgUnit.orgUnitSearchEditText.setText(null);
                localBinding.filterOrgUnit.orgUnitHint.setText(null);
                ouFilterAdapter.notifyDataSetChanged();
            }
        });

        localBinding.filterOrgUnit.ouTreeButton.setOnClickListener(view -> {
                localBinding.root.clearFocus();
                FilterManager.getInstance().getOuTreeProcessor().onNext(true);
        });
    }
}
