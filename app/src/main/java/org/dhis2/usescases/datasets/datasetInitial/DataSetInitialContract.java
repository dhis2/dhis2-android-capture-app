package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.List;

public class DataSetInitialContract {

    public enum Action {
        ACTION_CREATE,
        ACTION_UPDATE,
        ACTION_CHECK
    }

    public interface View extends AbstractActivityContracts.View {

        void setAccessDataWrite(Boolean canWrite);

        void setData(DataSetInitialModel dataSetInitialModel);

        void showOrgUnitDialog(List<OrganisationUnit> data);

        void showPeriodSelector(PeriodType periodType, List<DateRangeInputPeriodModel> periods, Integer openFuturePeriods);

        void showCatComboSelector(String catOptionUid, List<CategoryOption> data);

        String getDataSetUid();

        OrganisationUnit getSelectedOrgUnit();

        Date getSelectedPeriod();

        List<String> getSelectedCatOptions();

        String getPeriodType();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void onBackClick();

        void onOrgUnitSelectorClick();

        void onReportPeriodClick(PeriodType periodType);

        void onCatOptionClick(String catOptionUid);

        void onActionButtonClick();
    }


}
