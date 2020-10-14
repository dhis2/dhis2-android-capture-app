package org.dhis2.utils.filters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.filter.FilterPresenter;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.utils.filters.ou.OUFilterAdapter;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.List;

class OrgUnitFilterHolder extends FilterHolder {

    private final FilterPresenter filterPresenter;
    private OrganisationUnit currentOrgUnit;

    OrgUnitFilterHolder(@NonNull ItemFilterOrgUnitBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, FiltersAdapter.ProgramType programType, FilterPresenter filterPresenter) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ORG_UNIT;
        this.programType = programType;
        this.filterPresenter = filterPresenter;
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

        localBinding.filterOrgUnit.orgUnitSearchEditText.setDropDownVerticalOffset(4);
        localBinding.filterOrgUnit.orgUnitSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                localBinding.filterOrgUnit.progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 3) {
                    List<OrganisationUnit> orgUnits = d2.organisationUnitModule().organisationUnits()
                            .byDisplayName().like("%" + charSequence + "%")
                            .blockingGet();
                    currentOrgUnit = !orgUnits.isEmpty() ? orgUnits.get(0) : null;
                    List<String> orgUnitsNames = new ArrayList<>();
                    for (OrganisationUnit orgUnit: orgUnits) {
                        orgUnitsNames.add(orgUnit.displayName());
                    }
                    if (!orgUnitsNames.isEmpty()) {
                        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_dropdown_item_1line, orgUnitsNames);
                        localBinding.filterOrgUnit.orgUnitSearchEditText.setAdapter(autoCompleteAdapter);
                        localBinding.filterOrgUnit.orgUnitSearchEditText.showDropDown();
                    }
                } else
                    localBinding.filterOrgUnit.orgUnitHint.setText(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                localBinding.filterOrgUnit.progress.setVisibility(View.GONE);
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
