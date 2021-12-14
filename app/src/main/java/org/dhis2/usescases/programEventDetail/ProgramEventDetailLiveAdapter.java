package org.dhis2.usescases.programEventDetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder;
import org.hisp.dhis.android.core.program.Program;

import kotlin.Pair;
import kotlin.Unit;

public class ProgramEventDetailLiveAdapter extends PagedListAdapter<EventViewModel, EventViewHolder> {

    public static DiffUtil.ItemCallback<EventViewModel> getDiffCallback(){
        return new DiffUtil.ItemCallback<EventViewModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull EventViewModel oldItem, @NonNull EventViewModel newItem) {
                return oldItem.getEvent().uid().equals(newItem.getEvent().uid());
            }

            @Override
            public boolean areContentsTheSame(@NonNull EventViewModel oldItem, @NonNull EventViewModel newItem) {
                return oldItem.equals(newItem);
            }
        };
    }

    private final Program program;
    private ProgramEventDetailViewModel eventViewModel;

    public ProgramEventDetailLiveAdapter(Program program,
                                         ProgramEventDetailViewModel eventViewModel,
                                         AsyncDifferConfig<EventViewModel> config) {
        super(config);
        this.eventViewModel = eventViewModel;
        this.program = program;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEventBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_event, parent, false);
        return new EventViewHolder(binding,
                program,
                eventUid -> {
                    eventViewModel.getEventSyncClicked().setValue(eventUid);
                    return Unit.INSTANCE;
                },
                (s, view) -> Unit.INSTANCE,
                (eventUid, orgUnitUid, eventStatus, view) -> {
                    eventViewModel.getEventClicked().setValue(new Pair<>(eventUid, orgUnitUid));
                    return Unit.INSTANCE;
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position), null, () -> {
            getItem(holder.getAdapterPosition()).toggleValueList();
            notifyItemChanged(holder.getAdapterPosition());
            return Unit.INSTANCE;
        });
    }
}
