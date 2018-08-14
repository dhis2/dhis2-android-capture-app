package com.dhis2.usescases.dataset.dataSetPeriod;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.dataset.DataSet;

/**
 * Created by frodriguez on 7/20/2018.
 */
public class DataSetPeriodContract {

    public interface View extends AbstractActivityContracts.View {

        void setDataSet(DataSet dataSet);
    }

    public interface Presenter {
        void init(DataSetPeriodContract.View view, String dataSetId);
        void onBackClick();
        void showFilter();
        void addDataSet();
    }
}
