package com.dhis2.usescases.main.trackentitylist;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dhis2.BR;
import com.dhis2.databinding.CellLayoutBinding;

/**
 * Created by frodriguez on 10/19/2017.
 */

class CellViewHolder extends RecyclerView.ViewHolder {

    ViewDataBinding binding;

    public CellViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TrackEntityListPresenter presenter, CellModel cell){
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.cell, cell);
        binding.executePendingBindings();
    }
}
