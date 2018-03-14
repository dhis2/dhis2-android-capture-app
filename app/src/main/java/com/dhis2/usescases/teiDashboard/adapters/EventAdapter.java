package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemEventBinding;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 29/11/2017.
 *
 */

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<ProgramStageModel> programStageList;
    private final TeiDashboardContracts.Presenter presenter;
    private List<EventModel> events;

    public EventAdapter(TeiDashboardContracts.Presenter presenter, List<ProgramStageModel> programStageList, List<EventModel> eventList) {
        this.events = new ArrayList<>();
        for (EventModel event : eventList)
            if (event.status() == EventStatus.ACTIVE || event.status() == EventStatus.COMPLETED)
                this.events.add(event);

        this.programStageList = programStageList;
        this.presenter = presenter;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemEventBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_event, parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        ProgramStageModel programStage = null;
        for (ProgramStageModel stage : programStageList)
            if (events.get(position).programStage().equals(stage.uid()))
                programStage = stage;
        holder.bind(presenter, events.get(position), programStage);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }
}
