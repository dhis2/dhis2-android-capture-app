package com.dhis2.usescases.main.trackentitylist;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.RowLayoutBinding;

/**
 * Created by frodriguez on 10/19/2017.
 */

class RowViewHolder extends RecyclerView.ViewHolder {
    ViewDataBinding binding;

    public RowViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TrackEntityListPresenter presenter, RowHeaderModel row){
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.row, row);
        binding.executePendingBindings();
    }
}
