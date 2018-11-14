package org.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<ProgramStageModel> programStageList;
    private final TeiDashboardContracts.Presenter presenter;
    private final EnrollmentModel enrollment;
    private List<EventModel> events;

    public EventAdapter(TeiDashboardContracts.Presenter presenter, List<ProgramStageModel> programStageList, List<EventModel> eventList, EnrollmentModel currentEnrollment) {
        this.events = eventList;
        this.enrollment = currentEnrollment;
        this.programStageList = programStageList;
        this.presenter = presenter;
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
            holder.bind(presenter, events.get(position), programStage, enrollment);
        else {
            Timber.e(new Throwable(),"Program stage %s does not belong to program %s",
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
