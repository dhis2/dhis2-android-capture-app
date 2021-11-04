package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.jetbrains.annotations.NotNull;

import java.util.SortedMap;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View {
        void showSnackBar();

        void goToTable(int numTable);

        void showAlertDialog(String title, String message);

        void highligthHeaderRow(int table, int row, boolean mandatory);

        void update(boolean modified);

        void setTableData(TableData tableData);

        void setDataSet(DataSet dataSet);

        void setSection(Section section);

        void updateTabLayout(int count);

        void renderIndicators(@NotNull SortedMap<String, String> indicators);
    }
}
