package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.List;

public class DataSetInitialContract {

    public enum Action {
        ACTION_CREATE,
        ACTION_UPDATE,
        ACTION_CHECK
    }

    public static final int ACTION_CREATE = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_CHECK = 2;


    public interface View extends AbstractActivityContracts.View {

        void setAccessDataWrite(Boolean canWrite);

        void setData(DataSetInitialModel dataSetInitialModel);

        void showOrgUnitDialog(List<OrganisationUnitModel> data);

        void showPeriodSelector(PeriodType periodType);

        void showCatComboSelector(String catOptionUid, List<CategoryOptionModel> data);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        /*Actions*/
        void onBackClick();

        void onOrgUnitSelectorClick();

        void onReportPeriodClick(PeriodType periodType);

        void onCatOptionClick(String catOptionUid);

        void onActionButtonClick(Action action);
    }


}
