package com.dhis2.usescases.programEventDetail;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailViewHolder extends RecyclerView.ViewHolder {

    ViewDataBinding binding;

    public ProgramEventDetailViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramEventDetailContract.Presenter presenter,
                     ProgramModel program,
                     String orgUnit,
                     List<String> attributes,
                     String stage,
                     String uid) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, program);
        binding.setVariable(BR.orgUnit, orgUnit);
        binding.setVariable(BR.attribute, attributes);
        binding.setVariable(BR.stage, stage);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(uid, program.uid()));
    }


}
