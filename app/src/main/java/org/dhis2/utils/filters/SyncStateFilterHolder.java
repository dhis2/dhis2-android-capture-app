package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterStateBinding;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.common.State;

class SyncStateFilterHolder extends FilterHolder {

    SyncStateFilterHolder(@NonNull ItemFilterStateBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, ProgramType programType) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.SYNC_STATE;
        this.programType = programType;
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_sync));
        filterTitle.setText(R.string.filters_title_state);
    }
}
