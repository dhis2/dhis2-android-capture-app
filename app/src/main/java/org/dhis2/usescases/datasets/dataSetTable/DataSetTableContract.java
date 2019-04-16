package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void setDataElements(Map<String, List<DataElementModel>> data, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> stringListMap);

        void setDataValue(List<DataSetTableModel> data);

        Boolean accessDataWrite();

        void showOptions(boolean open);

        void showOrgUnitDialog(List<OrganisationUnitModel> data);

        void showPeriodSelector(PeriodType periodType);

        OrganisationUnitModel getSelectedOrgUnit();

        Date getSelectedPeriod();

        String getDataSetUid();

        String getOrgUnitName();

        void showCatComboSelector(String catOptionUid, List<CategoryOption> data);

        void setData(DataSetInitialModel dataSetInitialModel);

        void goToTable(int numTable);

        void setCurrentNumTables(int numTables);

        void renderDetails(DataSetInitialModel dataSetInitialModel);

        Consumer<Boolean> isDataSetOpen();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void onBackClick();

        void init(View view, String orgUnitUid, String periodTypeName, String catCombo, String periodFinalDate, String periodId);

        String getOrgUnitUid();
        String getPeriodTypeName();
        String getPeriodFinalDate();
        String getCatCombo();
        String getPeriodId();
        void optionsClick();

        void onOrgUnitSelectorClick();

        void onReportPeriodClick();

        void onRefreshClick();

        void onCatOptionClick(String catOptionUid);

        void onClickSelectTable(int numTable);

    }

}
