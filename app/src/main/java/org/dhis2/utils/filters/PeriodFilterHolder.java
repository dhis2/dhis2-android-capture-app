package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterPeriodBinding;
import org.dhis2.utils.filters.sorting.SortingItem;

class PeriodFilterHolder extends FilterHolder {

    private ItemFilterPeriodBinding localBinding;

    PeriodFilterHolder(@NonNull ItemFilterPeriodBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, ProgramType programType) {
        super(binding, openedFilter, sortingItem);
        localBinding = binding;
        filterType = Filters.PERIOD;
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_calendar_positive));
        this.programType = programType;
    }

    @Override
    public void bind() {
        super.bind();
    }

    public FilterHolder updateLabel(String eventDateLabel) {
        if (eventDateLabel != null) {
            filterTitle.setText(eventDateLabel);
        }
        return this;
    }

    @Override
    public void bind(FilterItem filterItem) {
        localBinding.setFilterItem(filterItem);
        updateLabel(((PeriodFilter) filterItem).getPeriodLabel());
        bind();
    }
}
