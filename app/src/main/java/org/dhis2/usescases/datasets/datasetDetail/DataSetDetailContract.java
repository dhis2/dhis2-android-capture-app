package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailContract;
import org.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class DataSetDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<DataSetDetailModel> dataSetDetailModels);

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showTimeUnitPicker();

        void showRageDatePicker();

        void renderError(String message);

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);

        void setOrgUnitFilter(StringBuilder orgUnitFilter);

        void showHideFilter();

        void apply();

        void setWritePermission(Boolean aBoolean);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String programId, Period period);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        ProgramModel getCurrentProgram();

        void addDataSet();

        void onBackClick();

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery);

        void clearCatComboFilters(String orgUnitQuery);

        void onDataSetClick(String eventId, String orgUnit);

        List<OrganisationUnitModel> getOrgUnits();

        Observable<List<TrackedEntityDataValueModel>> getDataSetDataValue(DataSetModel event);

        Observable<List<String>> getDataSetDataValueNew(DataSetDetailModel event);

        void showFilter();

        void getDataSets(Date fromDate, Date toDate, String orgUnitQuery);

        void getOrgUnits(Date date);

        void getDataSetWithDates(List<Date> dates, Period period, String orgUnitQuery);

    }
}
