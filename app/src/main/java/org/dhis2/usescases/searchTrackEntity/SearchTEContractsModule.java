package org.dhis2.usescases.searchTrackEntity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.dhis2.form.model.RowAction;
import org.dhis2.maps.model.EventUiComponentModel;
import org.dhis2.maps.model.StageStyle;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.commons.data.SearchTeiModel;
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

        void setTutorial();

        void setProgramColor(String data, String programUid);

        String fromRelationshipTEI();

        void setFabIcon(boolean needsSearch);

        void showHideFilter();

        void showHideFilterGeneral();

        void updateFilters(int totalFilters);

        void closeFilters();

        void openOrgUnitTreeSelector();

        void showPeriodRequest(Pair<FilterManager.PeriodRequest, Filters> periodRequest);

        void clearFilters();

        Consumer<D2Progress> downloadProgress();

        void openDashboard(String teiUid, String programUid, String enrollmentUid);

        void showBreakTheGlass(String teiUid, String enrollmentUid);

        void goToEnrollment(String enrollmentUid, String programUid);

        void onBackClicked();

        void couldNotDownload(String typeName);

        void setInitialFilters(List<FilterItem> filtersToDisplay);

        void showClearSearch(boolean empty);

        void hideFilter();

        void showSyncDialog(String teiUid);
    }

    public interface Presenter {

        void init();

        void onDestroy();

        void setProgram(Program programSelected);

        void onBackClick();

        void onClearClick();

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

        void onSyncIconClick(String teiUid);

        void showFilter();

        void showFilterGeneral();

        void clearFilterClick();

        void getMapData();

        Drawable getSymbolIcon();

        Drawable getEnrollmentSymbolIcon();

        HashMap<String, StageStyle> getProgramStageStyle();

        int getTEIColor();

        int getEnrollmentColor();

        void checkFilters(boolean listResultIsOk);

        void deleteRelationship(String relationshipUid);

        void setProgramForTesting(Program program);

        void clearOtherFiltersIfWebAppIsConfig();

        void setOpeningFilterToNone();

        void populateList(List<FieldUiModel> list);

        void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits);

        void setAttributesEmpty(Boolean attributesEmpty);

    }
}
