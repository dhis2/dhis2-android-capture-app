package com.dhis2.usescases.main.trackentitylist;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.ColumnLayoutBinding;

/**
 * Created by frodriguez on 10/19/2017.
 */

class ColumnViewHolder extends RecyclerView.ViewHolder {
    ViewDataBinding binding;

    public ColumnViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TrackEntityListPresenter presenter, ColumnHeaderModel column){
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.column, column);
        binding.executePendingBindings();
    }
}
