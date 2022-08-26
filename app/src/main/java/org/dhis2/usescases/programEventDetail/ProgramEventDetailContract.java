package org.dhis2.usescases.programEventDetail;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.workingLists.WorkingListItem;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(Program programModel);

        void renderError(String message);

        void showHideFilter();

        void setWritePermission(Boolean aBoolean);

        void showFilterProgress();

        void updateFilters(int totalFilters);

        void openOrgUnitTreeSelector();

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void startNewEvent();

        void navigateToEvent(String eventId, String orgUnit);

        void showSyncDialog(String uid);

        void showCatOptComboDialog(String catComboUid);

        void setFilterItems(List<FilterItem> programFilters);

        void hideFilters();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init();

        void addEvent();

        void onBackClick();

        void showFilter();

        void onSyncIconClick(String uid);

        void clearFilterClick();

        void filterCatOptCombo(String selectedCatOptionCombo);

        Program getProgram();

        FeatureType getFeatureType();

        List<WorkingListItem> workingLists();

        void clearOtherFiltersIfWebAppIsConfig();

        void setOpeningFilterToNone();

        String getStageUid();

        void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits);

        void trackEventProgramAnalytics();

        void trackEventProgramMap();
    }
}
