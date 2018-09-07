package org.dhis2.usescases.programEventDetail;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramEventBinding;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailAdapter extends RecyclerView.Adapter<ProgramEventDetailViewHolder> {

    private ProgramEventDetailContract.Presenter presenter;
    private List<EventModel> events;

    ProgramEventDetailAdapter(ProgramEventDetailContract.Presenter presenter) {
        this.presenter = presenter;
        this.events = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProgramEventDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramEventBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_program_event, parent, false);
        return new ProgramEventDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramEventDetailViewHolder holder, int position) {
        EventModel event = events.get(position);
        holder.bind(presenter, event);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void setEvents(List<EventModel> events){
      /*  Collections.sort(this.events, (ob1, ob2) -> ob2.lastUpdated().compareTo(ob1.lastUpdated()));
        Collections.sort(events, (ob1, ob2) -> ob2.lastUpdated().compareTo(ob1.lastUpdated()));*/

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProgramEventDiffCallback(this.events, events));
        this.events.clear();
        this.events.addAll(events);
        diffResult.dispatchUpdatesTo(this);
    }
}
