package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemEventBinding;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<ProgramStage> programStageList;
    private final TEIDataContracts.Presenter presenter;
    private final Enrollment enrollment;
    private final Program program;
    private List<Event> events;

    public EventAdapter(TEIDataContracts.Presenter presenter, List<ProgramStage> programStageList, List<Event> eventList, Enrollment currentEnrollment, Program currentProgram) {
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
        ProgramStage programStage = null;
        for (ProgramStage stage : programStageList)
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

    public void swapItems(List<Event> events) {
        this.events.clear();
        this.events.addAll(events);
        notifyDataSetChanged();
    }
}
