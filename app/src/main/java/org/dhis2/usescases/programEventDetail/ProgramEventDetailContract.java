package org.dhis2.usescases.programEventDetail;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

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

        void setMap(List<SymbolOptions> options);

        void setEventInfo(Pair<ProgramEventViewModel,LatLng> programEventViewModel);

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void updateDateFilter(List<DatePeriod> datePeriodList);

        void addEvent();

        void onBackClick();

        void onEventClick(String eventId, String orgUnit);

        void showFilter();

        void onSyncIconClick(String uid);

        void getEventInfo(String eventUid, LatLng latLng);

        void getMapData();
    }
}
