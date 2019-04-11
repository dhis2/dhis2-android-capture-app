package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemEventBinding;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<ProgramStageModel> programStageList;
    private final TEIDataContracts.Presenter presenter;
    private final EnrollmentModel enrollment;
    private final ProgramModel program;
    private List<EventModel> events;

    public EventAdapter(TEIDataContracts.Presenter presenter, List<ProgramStageModel> programStageList, List<EventModel> eventList, EnrollmentModel currentEnrollment, ProgramModel currentProgram) {
        this.events = eventList;
        this.enrollment = currentEnrollment;
        this.programStageList = programStageList;
        this.presenter = presenter;
        this.program = currentProgram;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_event, parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        ProgramStageModel programStage = null;
        for (ProgramStageModel stage : programStageList)
            if (Objects.equals(events.get(position).programStage(), stage.uid()))
                programStage = stage;
        if (programStage != null)
            holder.bind(presenter, events.get(position), programStage, enrollment, program);
        else {
            Timber.e(new Throwable(), "Program stage %s does not belong to program %s",
                    events.get(position).programStage(), enrollment.program());
        }
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void swapItems(List<EventModel> events) {
        this.events.clear();
        this.events.addAll(events);
        notifyDataSetChanged();
    }
}
