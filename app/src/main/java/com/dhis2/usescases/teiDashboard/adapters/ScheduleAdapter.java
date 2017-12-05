package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemScheduleBinding;

import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

    private List<Event> events;
    private final List<ProgramStageModel> programStageList;
    private Filter currentFilter = Filter.ALL;

    public enum Filter {
        OVERDUE, SCHEDULE, ALL
    }

    public ScheduleAdapter(List<ProgramStageModel> programStageList, List<Event> eventList) {
        this.programStageList = programStageList;

        this.events = new ArrayList<>();
        this.events.addAll(eventList);

        Collections.sort(events, (eventA, eventB) -> eventB.eventDate().compareTo(eventA.eventDate()));

    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        ProgramStageModel programStage = null;
        for (ProgramStageModel stage : programStageList)
            if (events.get(position).programStage().equals(stage.uid()))
                programStage = stage;
        holder.bind(events.get(position), position == 0, position == events.size() - 1, programStage);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public Filter filter() {
        switch (currentFilter) {
            case ALL:
                currentFilter = Filter.OVERDUE;
                break;
            case OVERDUE:
                currentFilter = Filter.SCHEDULE;
                break;
            case SCHEDULE:
                currentFilter = Filter.ALL;
                break;
        }
        return currentFilter;
    }
}
