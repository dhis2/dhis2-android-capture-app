package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface SearchRepository {

    @NonNull
    Observable<List<TrackedEntityAttributeModel>> programAttributes(String programId);

    Observable<List<TrackedEntityAttributeModel>> programAttributes();

    Observable<List<OptionModel>> optionSet(String optionSetId);

    Observable<List<ProgramModel>> programsWithRegistration(String programTypeId);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesOffline(@NonNull ProgramModel selectedProgram,
                                                                     @NonNull List<String> orgUnits,
                                                                     @Nullable HashMap<String, String> queryData);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesAll(@NonNull ProgramModel selectedProgram,
                                                                 @NonNull List<String> orgUnits,
                                                                 @Nullable HashMap<String, String> queryData);

    @NonNull
    Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam,Date enrollmentDate);

    Observable<List<OrganisationUnitModel>> getOrgUnits(@Nullable String selectedProgramUid);

    Flowable<List<SearchTeiModel>> transformIntoModel(List<SearchTeiModel> teiList, @Nullable ProgramModel selectedProgram);

    String getProgramColor(@NonNull String programUid);

    Observable<List<TrackedEntityAttributeModel>> trackedEntityTypeAttributes();
}
