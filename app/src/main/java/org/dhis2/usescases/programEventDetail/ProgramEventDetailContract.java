package org.dhis2.usescases.programEventDetail;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
 */

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(Program programModel);

        void renderError(String message);

        void showHideFilter();

        void setWritePermission(Boolean aBoolean);

        void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData);

        void setOptionComboAccess(Boolean canCreateEvent);

        void updateFilters(int totalFilters);

        void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos);

        void openOrgUnitTreeSelector();

        Consumer<kotlin.Pair<FeatureCollection, BoundingBox>> setMap();

        void setEventInfo(Pair<ProgramEventViewModel,LatLng> programEventViewModel);

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void clearFilters();

        Consumer<FeatureType> setFeatureType();

        void startNewEvent();

        boolean isMapVisible();

        void navigateToEvent(String eventId, String orgUnit);

        void showSyncDialog(String uid);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init();

        void addEvent();

        void onBackClick();

        void onEventClick(String eventId, String orgUnit);

        void showFilter();

        void onSyncIconClick(String uid);

        void getEventInfo(String eventUid, LatLng latLng);

        void getMapData();

        void clearFilterClick();
    }
}
