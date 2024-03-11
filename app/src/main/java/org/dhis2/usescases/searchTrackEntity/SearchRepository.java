package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.filters.sorting.SortingItem;
import org.dhis2.data.search.SearchParametersModel;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface SearchRepository {

    Observable<List<Program>> programsWithRegistration(String programTypeId);

    void clearFetchedList();

    @NonNull
    Flowable<List<SearchTeiModel>> searchTeiForMap(SearchParametersModel searchParametersModel, boolean isOnline);

    @NonNull
    Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam, @Nullable String fromRelationshipUid);

    Observable<List<OrganisationUnit>> getOrgUnits(@Nullable String selectedProgramUid);

    String getProgramColor(@NonNull String programUid);

    Observable<TrackedEntityType> getTrackedEntityType(String trackedEntityUid);

    TrackedEntityType getTrackedEntityType();

    List<EventViewModel> getEventsForMap(List<SearchTeiModel> teis);

    Observable<D2Progress> downloadTei(String teiUid);

    TeiDownloadResult download(String teiUid, @Nullable String enrollmentUid, String reason);

    SearchTeiModel transform(TrackedEntitySearchItem searchItem, @Nullable Program selectedProgram, boolean offlineOnly, SortingItem sortingItem);

    TrackedEntitySearchCollectionRepository getFilteredRepository(SearchParametersModel searchParametersModel);

    void setCurrentProgram(@Nullable String currentProgram);

    boolean programStagesHaveCoordinates(String programUid);

    boolean teTypeAttributesHaveCoordinates(String typeId);

    boolean programAttributesHaveCoordinates(String programUid);

    boolean eventsHaveCoordinates(String programUid);

    List<AnalyticsDhisVisualizationsGroup> getProgramVisualizationGroups(String programUid);

    @Nullable
    Program getProgram(@Nullable String programUid);

    @Nullable
    String currentProgram();

    @NotNull Map<String, String> filterQueryForProgram(@NotNull Map<String, String> queryData, @org.jetbrains.annotations.Nullable String programUid);

    boolean canCreateInProgramWithoutSearch();

    void setCurrentTheme(@org.jetbrains.annotations.Nullable ProgramSpinnerModel selectedProgram);

    List<String> trackedEntityTypeFields();

    boolean filtersApplyOnGlobalSearch();
}
