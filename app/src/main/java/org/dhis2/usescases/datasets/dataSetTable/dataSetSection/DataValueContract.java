package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedMap;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View {
        void showSnackBar();

        void update(boolean modified);

        void setTableData(TableData tableData);

        void updateTabLayout(int count);

        void renderIndicators(@NotNull SortedMap<String, String> indicators);

        void updateData(RowAction rowAction, @Nullable String catCombo);

        void onValueProcessed();

        void clearTables();
    }
}
