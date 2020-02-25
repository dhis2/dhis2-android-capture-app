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
    private final Program program;
    private final Enrollment enrollment;
    private final TEIDataContracts.Presenter presenter;
    private ItemEventBinding binding;

    EventViewHolder(ItemEventBinding binding, Program program, Enrollment enrollment, TEIDataContracts.Presenter presenter) {
        super(binding.getRoot());
        this.binding = binding;
        this.program = program;
        this.enrollment = enrollment;
        this.presenter = presenter;
    }

    public void bind(EventViewModel eventModel) {
        ProgramStage programStage = eventModel.getStage();
        Event event = eventModel.getEvent();
        binding.setEvent(eventModel.getEvent());
        binding.setStage(eventModel.getStage());

        binding.setEnrollment(enrollment);


        binding.setVariable(BR.enrollment, enrollment);
        binding.setVariable(BR.program, program);
        binding.executePendingBindings();

        String date = DateUtils.getInstance().getPeriodUIString(programStage.periodType(), event.eventDate() != null ? event.eventDate() : event.dueDate(), Locale.getDefault());
        binding.eventDate.setText(date);

        itemView.setOnClickListener(view -> {
            switch (eventModel.getEvent().status()) {
                case SCHEDULE:
                case OVERDUE:
                case SKIPPED:
                    presenter.onScheduleSelected(event.uid(), binding.sharedView);
                    break;
                case VISITED:
                    break;
                case ACTIVE:
                case COMPLETED:
                    presenter.onEventSelected(event.uid(), event.status(), binding.sharedView);
                    break;
            }
        });
    }
}
