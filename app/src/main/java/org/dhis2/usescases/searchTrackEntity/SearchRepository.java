package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.dhis2.data.search.SearchParametersModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.dhis2.commons.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface SearchRepository {

    Observable<List<FieldUiModel>> searchFields(@Nullable String programUid, Map<String, String> currentSearchValues);

    Observable<List<Program>> programsWithRegistration(String programTypeId);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntities(SearchParametersModel searchParametersModel, boolean isOnline);

    @NonNull
    Flowable<List<SearchTeiModel>> searchTeiForMap(SearchParametersModel searchParametersModel, boolean isOnline);

    SearchTeiModel getTrackedEntityInfo(String teiUid, Program selectedProgram, SortingItem sortingItem);

    @NonNull
    Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam, Date enrollmentDate, @Nullable String fromRelationshipUid);

    Observable<List<OrganisationUnit>> getOrgUnits(@Nullable String selectedProgramUid);

    String getProgramColor(@NonNull String programUid);


    Observable<TrackedEntityType> getTrackedEntityType(String trackedEntityUid);

    List<EventViewModel> getEventsForMap(List<SearchTeiModel> teis);

    EventViewModel getEventInfo(String enrollmentUid);

    Observable<D2Progress> downloadTei(String teiUid);

    void setCurrentProgram(@Nullable String currentProgram);
    boolean programHasAnalytics();
    boolean programHasCoordinates();
}
