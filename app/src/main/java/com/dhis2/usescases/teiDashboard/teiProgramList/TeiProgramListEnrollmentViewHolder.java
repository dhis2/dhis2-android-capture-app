package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.databinding.ItemTeiProgramsEnrollmentBinding;
import com.dhis2.databinding.ItemTeiProgramsEnrollmentInactiveBinding;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class TeiProgramListEnrollmentViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;

    TeiProgramListEnrollmentViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiProgramListContract.Presenter presenter, EnrollmentModel enrollment) {
        if (binding instanceof ItemTeiProgramsEnrollmentBinding || binding instanceof ItemTeiProgramsEnrollmentInactiveBinding){
            binding.setVariable(BR.enrollment, enrollment);
        }

        binding.setVariable(BR.presenter, presenter);
        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onEnrollmentClick(enrollment.uid()));
    }


}
