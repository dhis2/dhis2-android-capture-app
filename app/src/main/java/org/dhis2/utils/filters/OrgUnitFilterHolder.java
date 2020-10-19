package org.dhis2.utils.filters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.filter.FilterPresenter;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.utils.filters.ou.OUFilterAdapter;
import org.dhis2.utils.filters.sorting.FilteredOrgUnitResult;
import org.dhis2.utils.filters.sorting.SortingItem;

import kotlin.Unit;

class OrgUnitFilterHolder extends FilterHolder {

    private ItemFilterOrgUnitBinding localBinding;
    private final FilterPresenter filterPresenter;

    OrgUnitFilterHolder(@NonNull ItemFilterOrgUnitBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, FiltersAdapter.ProgramType programType, FilterPresenter filterPresenter) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ORG_UNIT;
        this.programType = programType;
        this.filterPresenter = filterPresenter;
        this.localBinding = binding;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_ou));
        filterTitle.setText(R.string.filters_title_org_unit);
        setUpAdapter();
    }

    private void setUpAdapter() {
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
                FilteredOrgUnitResult filteredOrgUnitResult = filterPresenter.getOrgUnitsByName(charSequence.toString());
                if (filteredOrgUnitResult.hasResult()) {
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_dropdown_item_1line, filteredOrgUnitResult.names());
                    localBinding.filterOrgUnit.orgUnitSearchEditText.setAdapter(autoCompleteAdapter);
                    localBinding.filterOrgUnit.orgUnitSearchEditText.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                localBinding.filterOrgUnit.progress.setVisibility(View.GONE);
            }
        });

        localBinding.filterOrgUnit.addButton.setOnClickListener(view ->
                filterPresenter.addOrgUnitToFilter(() -> {
                    localBinding.filterOrgUnit.orgUnitSearchEditText.setText(null);
                    ouFilterAdapter.notifyDataSetChanged();
                    return Unit.INSTANCE;
                }));

        localBinding.filterOrgUnit.ouTreeButton.setOnClickListener(view -> {
            localBinding.root.clearFocus();
            filterPresenter.onOpenOrgUnitTreeSelector();
        });
    }
}
