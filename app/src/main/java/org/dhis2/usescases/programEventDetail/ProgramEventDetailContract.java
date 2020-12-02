package org.dhis2.usescases.programEventDetail;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.dhis2.utils.filters.FilterItem;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.workingLists.WorkingListItem;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.functions.Consumer;

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(Program programModel);

        void renderError(String message);

        void showHideFilter();

        void setWritePermission(Boolean aBoolean);

        void showFilterProgress();

        void setLiveData(LiveData<PagedList<EventViewModel>> pagedListLiveData);

        void setOptionComboAccess(Boolean canCreateEvent);

        void updateFilters(int totalFilters);

        void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos);

        void openOrgUnitTreeSelector();

        void setMap(ProgramEventMapData mapData);

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void setFeatureType(FeatureType featureType);

        void startNewEvent();

        void updateEventCarouselItem(ProgramEventViewModel programEventViewModel);

        boolean isMapVisible();

        void navigateToEvent(String eventId, String orgUnit);

        void showSyncDialog(String uid);

        void showCatOptComboDialog(String catComboUid);

        void setFilterItems(List<FilterItem> programFilters);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init();

        void addEvent();

        void onBackClick();

        void onEventClick(String eventId, String orgUnit);

        void showFilter();

        void onSyncIconClick(String uid);

        void getEventInfo(String eventUid);

        void getMapData();

        void clearFilterClick();

        void filterCatOptCombo(String selectedCatOptionCombo);

        Program getProgram();

        FeatureType getFeatureType();

        List<WorkingListItem> workingLists();
    }
}
