package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import java.util.List;

/**
 * Created by ppajuelo on 29/01/2018.
 *
 */

class TeiProgramListDiffCallback extends DiffUtil.Callback {

    @NonNull
    private List<EnrollmentModel> oldList;
    @NonNull
    private List<EnrollmentModel> newList;

    TeiProgramListDiffCallback(@NonNull List<EnrollmentModel> oldList, @NonNull List<EnrollmentModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).uid()
                .equals(newList.get(newItemPosition).uid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition)
                .equals(newList.get(newItemPosition));
    }
}
