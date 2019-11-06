package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSet;

import java.util.List;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void setSections(List<String> sections);

        void setDataValue(List<DataSetTableModel> data);

        Boolean accessDataWrite();

        void showOptions(boolean open);

        String getDataSetUid();

        String getOrgUnitName();

        void goToTable(int numTable);

        void renderDetails(DataSet dataSet, String catcomboName);

        void isDataSetOpen(boolean dataSetIsOpen);

        void setDataSetState(State state);

        void runSmsSubmission();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void onBackClick();
        void onSyncClick();

        void init(View view, String orgUnitUid, String periodTypeName, String catCombo, String periodFinalDate, String periodId);

        String getOrgUnitUid();
        String getPeriodTypeName();
        String getPeriodFinalDate();
        String getCatCombo();
        String getPeriodId();
        void optionsClick();

        void onClickSelectTable(int numTable);

        String getCatOptComboFromOptionList(List<String> catOpts);
    }

}
