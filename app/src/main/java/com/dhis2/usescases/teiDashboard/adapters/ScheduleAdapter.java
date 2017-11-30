package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemScheduleBinding;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.List;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

    private List<EventModel> events;

    public ScheduleAdapter(List<EventModel> eventList) {
        this.events = eventList;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        holder.bind(events.get(position), position == 0, position == events.size() - 1);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }
}
