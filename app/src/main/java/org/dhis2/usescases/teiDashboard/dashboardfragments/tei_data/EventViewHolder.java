package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Locale;

/**
 * Created by ppajuelo on 29/11/2017.
 */

class EventViewHolder extends RecyclerView.ViewHolder {
    private ItemEventBinding binding;

    EventViewHolder(ItemEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TEIDataContracts.Presenter presenter, Event eventModel, ProgramStage programStage, Enrollment enrollment, Program program) {
        binding.setVariable(BR.event, eventModel);
        binding.setVariable(BR.stage, programStage);
        binding.setVariable(BR.enrollment, enrollment);
        binding.setVariable(BR.program, program);
        binding.executePendingBindings();

        String date = DateUtils.getInstance().getPeriodUIString(programStage.periodType(), eventModel.eventDate() != null ? eventModel.eventDate() : eventModel.dueDate(), Locale.getDefault());
        binding.eventDate.setText(date);

        itemView.setOnClickListener(view -> {
            switch (eventModel.status()) {
                case SCHEDULE:
                case OVERDUE:
                case SKIPPED:
                    presenter.onScheduleSelected(eventModel.uid(), binding.sharedView);
                    break;
                case VISITED:
                    break;
                case ACTIVE:
                case COMPLETED:
                    presenter.onEventSelected(eventModel.uid(), eventModel.status(), binding.sharedView);
                    break;
            }
        });
    }
}
