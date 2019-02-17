package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;

public class DataValueContract {

    public interface View {

    }

    public interface Presenter extends AbstractActivityContracts.Presenter{
        void init(View view);

        void insertDataValues(List<DataValueModel> dataValues);

        void save();
    }
}
