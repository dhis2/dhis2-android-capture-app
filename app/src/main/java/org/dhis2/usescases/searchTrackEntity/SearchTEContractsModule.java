package org.dhis2.usescases.searchTrackEntity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.dhis2.form.model.FieldUiModel;
import org.dhis2.uicomponents.map.model.EventUiComponentModel;
import org.dhis2.uicomponents.map.model.StageStyle;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import kotlin.Pair;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {

        void setPrograms(List<Program> programModels);

        void setFiltersVisibility(boolean showFilters);

        void clearList(String uid);

        void clearData();

        void showFilterProgress();

        void setTutorial();

        void setProgramColor(String data);

        String fromRelationshipTEI();

        void setLiveData(LiveData<PagedList<SearchTeiModel>> liveData);

        void setFabIcon(boolean needsSearch);

        void showHideFilter();

        void showHideFilterGeneral();

        void updateFilters(int totalFilters);

        void closeFilters();

        void openOrgUnitTreeSelector();

        void showPeriodRequest(Pair<FilterManager.PeriodRequest, Filters> periodRequest);

        void clearFilters();

        void updateFiltersSearch(int totalFilters);

        void setMap(TrackerMapData trackerMapData);

        Consumer<D2Progress> downloadProgress();

        boolean isMapVisible();

        void openDashboard(String teiUid, String programUid, String enrollmentUid);

        void showBreakTheGlass(String teiUid, String enrollmentUid);

        void goToEnrollment(String enrollmentUid, String programUid);

        void onBackClicked();

        void couldNotDownload(String typeName);

        void setFormData(List<FieldUiModel> data);

        void setInitialFilters(List<FilterItem> filtersToDisplay);

        void showClearSearch(boolean empty);

        void hideFilter();

        void updateNavigationBar();

        void displayMinNumberOfAttributesMessage(int minAttributes);
    }

    public interface Presenter {

        void init(String trackedEntityType);

        void onDestroy();

        void setProgram(Program programSelected);

        void onBackClick();

        void onClearClick();

        void onFabClick(boolean needsSearch);

        void onEnrollClick();

        void onTEIClick(String teiUid, String enrollmentUid, boolean isOnline);

        TrackedEntityType getTrackedEntityName();

        TrackedEntityType getTrackedEntityType(String trackedEntityTypeUid);

        Program getProgram();

        void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online);

        void downloadTei(String teiUid, String enrollmentUid);

        void downloadTeiWithReason(String teiUid, String enrollmentUid, String reason);

        void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid);

        Observable<List<OrganisationUnit>> getOrgUnits();

        String getProgramColor(String uid);

        SearchMessageResult getMessage(List<SearchTeiModel> list);

        HashMap<String, String> getQueryData();

        void onSyncIconClick(String teiUid);

        void showFilter();

        void showFilterGeneral();

        void resetSearch();

        void clearFilterClick();

        void closeFilterClick();

        void getMapData();

        void getListData();

        Drawable getSymbolIcon();

        void getEnrollmentMapData();

        Drawable getEnrollmentSymbolIcon();

        HashMap<String, StageStyle> getProgramStageStyle();

        String nameOUByUid(String uid);

        int getTEIColor();

        int getEnrollmentColor();

        void checkFilters(boolean listResultIsOk);

        void restoreQueryData(HashMap<String, String> queryData);

        void deleteRelationship(String relationshipUid);

        SearchTeiModel getTeiInfo(String teiUid);

        EventUiComponentModel getEventInfo(String eventUid, String teiUid);

        void setProgramForTesting(Program program);

        void clearOtherFiltersIfWebAppIsConfig();

        void setOpeningFilterToNone();

        void populateList(List<FieldUiModel> list);

        void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits);

        boolean selectedProgramMinNumberOfAttributesCheck();
    }
}
