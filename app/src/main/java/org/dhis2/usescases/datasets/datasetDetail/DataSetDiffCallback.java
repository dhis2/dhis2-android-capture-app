package org.dhis2.usescases.datasets.datasetDetail;

import android.support.v7.util.DiffUtil;

import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;

public class DataSetDiffCallback extends DiffUtil.Callback {

    private List<DataSetDetailModel> oldList;
    private List<DataSetDetailModel> newList;

    public DataSetDiffCallback(List<DataSetDetailModel> oldList, List<DataSetDetailModel> newList) {
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
        return oldList.get(oldItemPosition).getUidDataSet()
                .equals(newList.get(newItemPosition).getUidDataSet());
    }
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition)
                .equals(newList.get(newItemPosition));
    }
}
