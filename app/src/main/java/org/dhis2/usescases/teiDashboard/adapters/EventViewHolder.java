package org.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

/**
 * Created by ppajuelo on 29/11/2017.
 *
 */

class EventViewHolder extends RecyclerView.ViewHolder {
    ItemEventBinding binding;

    EventViewHolder(ItemEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiDashboardContracts.Presenter presenter, EventModel eventModel, ProgramStageModel programStage, EnrollmentModel enrollment) {
        binding.setVariable(BR.event, eventModel);
        binding.setVariable(BR.stage, programStage);
        binding.setVariable(BR.enrollment, enrollment);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onEventSelected(eventModel.uid(), binding.sharedView));
    }
}
