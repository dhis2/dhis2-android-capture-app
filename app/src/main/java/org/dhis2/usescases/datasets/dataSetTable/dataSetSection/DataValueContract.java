package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;

import java.util.List;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View {
        void showSnackBar();

        void goToTable(int numTable);

        void showAlertDialog(String title, String message);

        boolean isOpenOrReopen();

        void setCompleteReopenText(Boolean isCompleted);

        void highligthHeaderRow(int table, int row, boolean mandatory);

        void update(boolean modified);

        void setTableData(DataTableModel dataTableModel, List<List<FieldViewModel>> fields, List<List<String>> cells, Boolean accessDataWrite);

        void setDataSet(DataSet dataSet);

        void setSection(Section section);

        void updateTabLayout(int count);

        void setDataAccess(boolean accessDataWrite);

        void showValidationRuleDialog();

        void showSuccessValidationDialog();

        void showErrorsValidationDialog();
    }
}
