package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.filter.FilterPresenter;
import org.dhis2.databinding.ItemFilterOrgUnitBinding;
import org.dhis2.utils.filters.sorting.SortingItem;

class OrgUnitFilterHolder extends FilterHolder {

    OrgUnitFilterHolder(@NonNull ItemFilterOrgUnitBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, ProgramType programType, FilterPresenter filterPresenter) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ORG_UNIT;
        this.programType = programType;
        binding.setFilterPresenter(filterPresenter);
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_ou));
        filterTitle.setText(R.string.filters_title_org_unit);
    }
}
