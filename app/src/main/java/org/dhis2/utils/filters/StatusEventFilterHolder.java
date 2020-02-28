package org.dhis2.utils.filters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterStatusBinding;
import org.hisp.dhis.android.core.event.EventStatus;

public class StatusEventFilterHolder extends FilterHolder {

    private final FiltersAdapter.ProgramType programType;

    StatusEventFilterHolder(@NonNull ItemFilterStatusBinding binding, ObservableField<Filters> openedFilter, FiltersAdapter.ProgramType programType) {
        super(binding, openedFilter);
        filterType = Filters.EVENT_STATUS;
        this.programType = programType;
    }

    @Override
    protected void bind() {
        super.bind();
        filterTitle.setText(R.string.filters_title_status);
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_status));

        ItemFilterStatusBinding localBinding = (ItemFilterStatusBinding) binding;

        if (programType == FiltersAdapter.ProgramType.EVENT) {
            localBinding.filterStatus.layoutScheduled.setVisibility(View.GONE);
            localBinding.filterStatus.layoutOverdue.setVisibility(View.GONE);
            localBinding.filterStatus.layoutSkipped.setVisibility(View.GONE);
        }

        localBinding.filterStatus.stateOpen.setChecked(FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.ACTIVE) ||
                FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.VISITED));
        localBinding.filterStatus.stateScheduled.setChecked(FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.SCHEDULE));
        localBinding.filterStatus.stateOverdue.setChecked(FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.OVERDUE));
        localBinding.filterStatus.stateCompleted.setChecked(FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.COMPLETED));
        localBinding.filterStatus.stateSkipped.setChecked(FilterManager.getInstance().getEventStatusFilters().contains(EventStatus.SKIPPED));

        localBinding.filterStatus.stateOpen.setOnCheckedChangeListener((compoundButton, b) -> FilterManager.getInstance().addEventStatus(!b, EventStatus.ACTIVE, EventStatus.VISITED));
        localBinding.filterStatus.stateScheduled.setOnCheckedChangeListener((compoundButton, b) -> FilterManager.getInstance().addEventStatus(!b, EventStatus.SCHEDULE));
        localBinding.filterStatus.stateOverdue.setOnCheckedChangeListener((compoundButton, b) -> FilterManager.getInstance().addEventStatus(!b, EventStatus.OVERDUE));
        localBinding.filterStatus.stateCompleted.setOnCheckedChangeListener((compoundButton, b) -> FilterManager.getInstance().addEventStatus(!b, EventStatus.COMPLETED));
        localBinding.filterStatus.stateSkipped.setOnCheckedChangeListener((compoundButton, b) -> FilterManager.getInstance().addEventStatus(!b, EventStatus.SKIPPED));
    }
}
