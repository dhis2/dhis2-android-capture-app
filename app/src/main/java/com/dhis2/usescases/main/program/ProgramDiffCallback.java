package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by ppajuelo on 29/01/2018.
 */

class ProgramDiffCallback extends DiffUtil.Callback {

    @NonNull
    private List<ProgramModel> oldList;
    @NonNull
    private List<ProgramModel> newList;

    ProgramDiffCallback(@NonNull List<ProgramModel> oldList, @NonNull List<ProgramModel> newList) {
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
