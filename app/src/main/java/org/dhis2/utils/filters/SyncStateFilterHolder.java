package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterStateBinding;
import org.hisp.dhis.android.core.common.State;

class SyncStateFilterHolder extends FilterHolder {

    SyncStateFilterHolder(@NonNull ItemFilterStateBinding binding, ObservableField<Filters> openedFilter) {
        super(binding, openedFilter);
        filterType = Filters.SYNC_STATE;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_sync));
        filterTitle.setText(R.string.filters_title_state);
        ItemFilterStateBinding localBinding = (ItemFilterStateBinding) binding;

        localBinding.filterState.stateSynced.setChecked(
                FilterManager.getInstance().getStateFilters().contains(State.SYNCED)
        );
        localBinding.filterState.stateNotSynced.setChecked(
                FilterManager.getInstance().getStateFilters().contains(State.TO_UPDATE) ||
                        FilterManager.getInstance().getStateFilters().contains(State.TO_POST) ||
                        FilterManager.getInstance().getStateFilters().contains(State.UPLOADING)
        );
        localBinding.filterState.stateError.setChecked(
                FilterManager.getInstance().getStateFilters().contains(State.ERROR) ||
                        FilterManager.getInstance().getStateFilters().contains(State.WARNING)
        );
        localBinding.filterState.stateSMS.setChecked(
                FilterManager.getInstance().getStateFilters().contains(State.SENT_VIA_SMS) ||
                        FilterManager.getInstance().getStateFilters().contains(State.SYNCED_VIA_SMS)
        );

        localBinding.filterState.stateSynced.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(!b, State.SYNCED));
        localBinding.filterState.stateNotSynced.setOnCheckedChangeListener((compoundButton, b) ->{
                    FilterManager.getInstance().addState(!b, State.TO_UPDATE);
                    FilterManager.getInstance().addState(!b, State.TO_POST);
                    FilterManager.getInstance().addState(!b, State.UPLOADING);
                });
        localBinding.filterState.stateError.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(!b, State.ERROR));
        localBinding.filterState.stateSMS.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(!b, State.SYNCED_VIA_SMS));
    }
}
