package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import io.reactivex.processors.FlowableProcessor;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View{
        void showSnackBar();

        void onComplete();

        void setPeriod(PeriodModel periodModel);

        void goToTable(int numTable);

        void showAlertDialog(String title, String message);

        void setListOptions(List<OptionModel> options);

        boolean isOpenOrReopen();

        void setCompleteReopenText(Boolean isCompleted);

        void highligthHeaderRow(int table, int row, boolean mandatory);

        void update(boolean modified);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter{
        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo, String section, String periodId);

        void complete();

        void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String section);
        void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment);
        Map<String, List<List<CategoryOptionModel>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map);

        List<List<String>> getCatOptionCombos(List<List<Pair<CategoryOptionModel, CategoryModel>>> listCategories, int num ,List<List<String>> result, List<String> current);

        void setCurrentNumTables(List<String> tablesNames);

        List<String> getCurrentNumTables();

        FlowableProcessor<RowAction> getProcessor();

        FlowableProcessor<Trio<String, String, Integer>> getProcessorOptionSet();

        void addCells(int table, List<List<FieldViewModel>> cells);

        DataInputPeriodModel checkHasInputPeriod();

        List<DataInputPeriodModel> getDataInputPeriodModel();
    }
}
