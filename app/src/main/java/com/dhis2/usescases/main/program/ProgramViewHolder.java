package com.dhis2.usescases.main.program;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramViewHolder extends RecyclerView.ViewHolder {

    ViewDataBinding binding;

    public ProgramViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramPresenter presenter, HomeViewModel bindableObject) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, bindableObject);
        binding.executePendingBindings();
    }

    public void bind(ProgramPresenter presenter, ProgramModel bindableObject) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, bindableObject);
        binding.executePendingBindings();
    }
}
