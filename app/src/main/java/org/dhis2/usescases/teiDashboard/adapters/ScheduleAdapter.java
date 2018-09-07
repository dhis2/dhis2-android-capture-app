package org.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemScheduleBinding;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

    private List<EventModel> events;
    private Filter currentFilter = Filter.ALL;

    public enum Filter {
        OVERDUE, SCHEDULE, ALL
    }

    public ScheduleAdapter() {
        this.events = new ArrayList<>();
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        holder.bind(events.get(position), position == 0, position == events.size() - 1, events.get(position).programStage());
    }

    @Override
    public int getItemCount() {
        return events.size();
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

    public void setScheduleEvents(List<EventModel> eventList) {
        this.events.clear();
        this.events.addAll(eventList);

        Collections.sort(events, (eventA, eventB) -> {

            if (eventA.status() == EventStatus.SCHEDULE && eventB.status() == EventStatus.SCHEDULE)
                return eventB.eventDate().compareTo(eventA.eventDate());
            if (eventA.status() == EventStatus.SCHEDULE && eventB.status() == EventStatus.SKIPPED)
                return eventB.dueDate().compareTo(eventA.eventDate());
            if (eventA.status() == EventStatus.SKIPPED && eventB.status() == EventStatus.SCHEDULE)
                return eventB.eventDate().compareTo(eventA.dueDate());
            else
                return eventB.dueDate().compareTo(eventA.dueDate());
        });

        notifyDataSetChanged();
    }
}
