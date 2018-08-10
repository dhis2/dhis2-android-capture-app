package com.dhis2.usescases.datasets.datasetDetail;

import android.support.v7.util.DiffUtil;

import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;

public class DataSetDiffCallback extends DiffUtil.Callback {

    private List<DataSetModel> oldList;
    private List<DataSetModel> newList;

    public DataSetDiffCallback(List<DataSetModel> oldList, List<DataSetModel> newList) {
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
