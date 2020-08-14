package org.dhis2.usescases.programEventDetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.databinding.ItemEventBinding;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.hisp.dhis.android.core.program.Program;

import kotlin.Unit;

public class ProgramEventDetailLiveAdapter extends PagedListAdapter<EventViewModel, EventViewHolder> {

    private static final DiffUtil.ItemCallback<EventViewModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<EventViewModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull EventViewModel oldItem, @NonNull EventViewModel newItem) {
            return oldItem.getEvent().uid().equals(newItem.getEvent().uid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull EventViewModel oldItem, @NonNull EventViewModel newItem) {
            return oldItem.equals(newItem);
        }
    };
    private final Program program;
    private ProgramEventDetailContract.Presenter presenter;

    public ProgramEventDetailLiveAdapter(Program program, ProgramEventDetailContract.Presenter presenter) {
        super(DIFF_CALLBACK);
        this.presenter = presenter;
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
                    presenter.onSyncIconClick(eventUid);
                    return Unit.INSTANCE;
                },
                (s, view) -> Unit.INSTANCE,
                (eventUid, orgUnitUid, eventStatus, view) -> {
                    presenter.onEventClick(eventUid, orgUnitUid);
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
