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
        filterTitle.setText("State");

        ItemFilterStateBinding localBinding = (ItemFilterStateBinding) binding;

        localBinding.filterState.stateSynced.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(State.SYNCED));
        localBinding.filterState.stateNotSynced.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(State.TO_UPDATE));
        localBinding.filterState.stateError.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(State.ERROR));
        localBinding.filterState.stateSMS.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addState(State.SYNCED_VIA_SMS));
    }
}
