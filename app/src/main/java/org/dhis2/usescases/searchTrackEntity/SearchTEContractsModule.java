package org.dhis2.usescases.searchTrackEntity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.common.FeatureType;
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
        void setForm(List<TrackedEntityAttribute> trackedEntityAttributes, @Nullable Program program, HashMap<String, String> queryData);

        void setPrograms(List<Program> programModels);

        void clearList(String uid);

        Flowable<RowAction> rowActionss();

        void clearData();

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

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void clearFilters();

        void updateFiltersSearch(int totalFilters);

        Consumer<FeatureType> featureType();

        Consumer<Pair<HashMap<String, FeatureCollection>, BoundingBox>> setMap();

        Consumer<D2Progress> downloadProgress();

        boolean isMapVisible();
    }

    public interface Presenter {

        void init(String trackedEntityType);

        void onDestroy();

        void setProgram(Program programSelected);

        void onBackClick();

        void onClearClick();

        void onFabClick(boolean needsSearch);

        void onEnrollClick();

        void onTEIClick(String TEIuid, boolean isOnline);

        TrackedEntityType getTrackedEntityName();

        Program getProgram();

        void addRelationship(@NonNull String teiUid, @Nullable String relationshipTypeUid, boolean online);

        void downloadTei(String teiUid);

        void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid);

        Observable<List<OrganisationUnit>> getOrgUnits();

        String getProgramColor(String uid);

        Trio<PagedList<SearchTeiModel>, String, Boolean> getMessage(PagedList<SearchTeiModel> list);

        HashMap<String, String> getQueryData();

        void initSearch();

        void onSyncIconClick(String teiUid);

        void showFilter();

        void showFilterGeneral();

        void clearFilterClick();

        void closeFilterClick();

        void getMapData();

        Drawable getSymbolIcon();

        void getEnrollmentMapData();

        Drawable getEnrollmentSymbolIcon();

        String nameOUByUid(String uid);

        int getTEIColor();

        int getEnrollmentColor();
    }
}
