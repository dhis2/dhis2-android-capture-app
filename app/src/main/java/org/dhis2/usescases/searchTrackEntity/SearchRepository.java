package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface SearchRepository {

    @NonNull
    Observable<List<TrackedEntityAttribute>> programAttributes(String programId);

    Observable<List<Program>> programsWithRegistration(String programTypeId);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntities(@Nullable Program selectedProgram,
                                                              @NonNull String trackedEntityType,
                                                              @NonNull List<String> orgUnits,
                                                              @Nonnull List<State> states,
                                                              @Nullable HashMap<String, String> queryData,
                                                              boolean isOnline);

    @NonNull
    Flowable<List<SearchTeiModel>> searchTeiForMap(@Nullable Program selectedProgram,
                                                        @NonNull String trackedEntityType,
                                                        @NonNull List<String> orgUnits,
                                                        @Nonnull List<State> states,
                                                        @Nullable HashMap<String, String> queryData,
                                                        boolean isOnline);

    @NonNull
    Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam, Date enrollmentDate);

    Observable<List<OrganisationUnit>> getOrgUnits(@Nullable String selectedProgramUid);

    String getProgramColor(@NonNull String programUid);

    Observable<List<TrackedEntityAttribute>> trackedEntityTypeAttributes();

    Observable<TrackedEntityType> getTrackedEntityType(String trackedEntityUid);

    Observable<List<OrganisationUnit>> getOrganisationUnits();
}
