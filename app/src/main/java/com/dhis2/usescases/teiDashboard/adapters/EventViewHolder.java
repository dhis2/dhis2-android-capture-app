package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.ItemEventBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.TeiDashboardPresenter;

import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.ProgramStageModel;

/**
 * Created by ppajuelo on 29/11/2017.
 */

class EventViewHolder extends RecyclerView.ViewHolder {
    ItemEventBinding binding;

    public EventViewHolder(ItemEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiDashboardContracts.Presenter presenter, Event eventModel, ProgramStageModel programStage) {
        binding.setVariable(BR.event, eventModel);
        binding.setVariable(BR.stage, programStage);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onEventSelected(eventModel.uid(), binding.sharedView));
    }
}
