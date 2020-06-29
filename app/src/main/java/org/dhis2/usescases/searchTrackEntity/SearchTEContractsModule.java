package org.dhis2.usescases.searchTrackEntity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.evrencoskun.tableview.filter.Filter;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage;
import org.dhis2.uicomponents.map.model.EventUiComponentModel;
import org.dhis2.uicomponents.map.model.StageStyle;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.Filters;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import kotlin.Pair;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {
        void setForm(List<TrackedEntityAttribute> trackedEntityAttributes,
                     @Nullable Program program,
                     HashMap<String, String> queryData,
                     List<ValueTypeDeviceRendering> renderingTypes);

        void setPrograms(List<Program> programModels);

        void setFiltersVisibility(boolean showFilters);

        void clearList(String uid);

        Flowable<RowAction> rowActionss();

        void clearData();

        void setTutorial();

        void showAssignmentFilter();

        void hideAssignmentFilter();

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

        Consumer<FeatureType> featureType();

        void setMap(List<SearchTeiModel> teis, HashMap<String, FeatureCollection> teiFeatureCollections, BoundingBox boundingBox, EventsByProgramStage events, List<EventUiComponentModel> eventUiComponentModels);

        Consumer<D2Progress> downloadProgress();

        boolean isMapVisible();

        void openDashboard(String teiUid, String programUid, String enrollmentUid);

        void goToEnrollment(String enrollmentUid, String programUid);

        void onBackClicked();
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

        Program getProgram();

        void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online);

        void downloadTei(String teiUid, String enrollmentUid);

        void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid);

        Observable<List<OrganisationUnit>> getOrgUnits();

        String getProgramColor(String uid);

        Trio<PagedList<SearchTeiModel>, String, Boolean> getMessage(PagedList<SearchTeiModel> list);

        HashMap<String, String> getQueryData();

        void onSyncIconClick(String teiUid);

        void showFilter();

        void showFilterGeneral();

        void clearFilterClick();

        void closeFilterClick();

        void getMapData();

        Drawable getSymbolIcon();

        void getEnrollmentMapData();

        Drawable getEnrollmentSymbolIcon();

        HashMap<String, StageStyle> getProgramStageStyle();

        String nameOUByUid(String uid);

        int getTEIColor();

        int getEnrollmentColor();

        void initAssignmentFilter();

        void checkFilters(boolean listResultIsOk);

        void restoreQueryData(HashMap<String, String> queryData);

        void deleteRelationship(String relationshipUid);
    }
}
