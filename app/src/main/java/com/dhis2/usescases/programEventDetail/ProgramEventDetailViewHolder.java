package com.dhis2.usescases.programEventDetail;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;

import org.hisp.dhis.android.core.event.EventModel;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;

    public ProgramEventDetailViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramEventDetailContract.Presenter presenter, EventModel event) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.event, event);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onEventClick(event.uid()));
    }


}
