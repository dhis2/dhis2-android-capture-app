package com.dhis2.usescases.programDetail;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

/**
 * Created by frodriguez on 10/31/2017.
 */

public class ProgramDetailViewHolder extends RecyclerView.ViewHolder {

    ViewDataBinding binding;

    public ProgramDetailViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramDetailPresenter presenter,
                     HomeViewModel program,
                     String orgUnit,
                     List<String> attributes){
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, program);
        binding.setVariable(BR.orgUnit, orgUnit);
        binding.setVariable(BR.attribute, attributes);
        binding.executePendingBindings();
    }


}
