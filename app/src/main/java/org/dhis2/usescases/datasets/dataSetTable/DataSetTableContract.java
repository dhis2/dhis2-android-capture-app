package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.usescases.general.AbstractActivityContracts;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo);
    }
}
