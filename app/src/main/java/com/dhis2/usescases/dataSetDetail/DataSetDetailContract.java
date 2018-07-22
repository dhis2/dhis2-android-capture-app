package com.dhis2.usescases.dataSetDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.List;

/**
 * Created by frodriguez on 7/20/2018.
 */
public class DataSetDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void setData(List<DataElement> aggregates);
    }

    public interface Presenter {
        void init();
    }
}
