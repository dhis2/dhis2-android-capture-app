package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.period.Period;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.processors.FlowableProcessor;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View{
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
    }

    public interface Presenter extends AbstractActivityContracts.Presenter{
        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo, String section, String periodId);

        void complete();

        void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment);

        Map<String, List<List<CategoryOption>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOption, Category>>>> map);

        List<List<String>> getCatOptionCombos(List<List<Pair<CategoryOption, Category>>> listCategories, int num ,List<List<String>> result, List<String> current);

        List<String> getCurrentNumTables();

        FlowableProcessor<RowAction> getProcessor();

        FlowableProcessor<Trio<String, String, Integer>> getProcessorOptionSet();

        DataInputPeriod checkHasInputPeriod();
    }
}
