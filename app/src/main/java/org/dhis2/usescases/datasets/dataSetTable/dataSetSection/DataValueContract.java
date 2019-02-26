package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;

public class DataValueContract {

    public interface View {
        void showSnackBar();

        void setPeriod(PeriodModel periodModel);

        void setDataInputPeriod(DataInputPeriodModel dataInputPeriod);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter{
        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo, String section);

        void save();

        void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String section);
        void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment);
        Map<String, List<List<CategoryOptionModel>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map);

        List<List<String>> getCatOptionCombos(List<List<Pair<CategoryOptionModel, CategoryModel>>> listCategories, int num ,List<List<String>> result, List<String> current);

        List<FieldViewModel> transformToFieldViewModels(List<DataSetTableModel> dataValues);


    }
}
