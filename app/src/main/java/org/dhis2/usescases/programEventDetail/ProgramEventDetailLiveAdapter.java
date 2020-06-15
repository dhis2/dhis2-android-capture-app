package org.dhis2.usescases.programEventDetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramEventBinding;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailLiveAdapter extends PagedListAdapter<ProgramEventViewModel, ProgramEventDetailViewHolder> {

    private static final DiffUtil.ItemCallback<ProgramEventViewModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProgramEventViewModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProgramEventViewModel oldItem, @NonNull ProgramEventViewModel newItem) {
            return oldItem.uid() == newItem.uid();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProgramEventViewModel oldItem, @NonNull ProgramEventViewModel newItem) {
            return oldItem.uid().equals(newItem.uid()) &&
                    oldItem.date().equals(newItem.date()) &&
                    oldItem.isExpired().equals(newItem.isExpired()) &&
                    oldItem.eventStatus().equals(newItem.eventStatus()) &&
                    oldItem.eventDisplayData().size() == newItem.eventDisplayData().size();
        }
    };
    private ProgramEventDetailContract.Presenter presenter;

    public ProgramEventDetailLiveAdapter(ProgramEventDetailContract.Presenter presenter) {
        super(DIFF_CALLBACK);
        this.presenter = presenter;
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
        holder.bind(presenter, getItem(position));
    }

   /* @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }*/

  /*  public void setEvents(List<ProgramEventViewModel> events, int currentPage) {

        if (currentPage == 0)
            this.events = new ArrayList<>();

        this.events.addAll(events);

        notifyDataSetChanged();

    }*/

   /* public void clearData() {
        this.events.clear();
    }*/
}
