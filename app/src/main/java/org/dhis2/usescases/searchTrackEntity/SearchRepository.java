package org.dhis2.usescases.searchTrackEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface SearchRepository {

    @NonNull
    Observable<List<TrackedEntityAttribute>> programAttributes(String programId);

    Observable<List<OptionModel>> optionSet(String optionSetId);

    Observable<List<Program>> programsWithRegistration(String programTypeId);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesOffline(@NonNull Program selectedProgram,
                                                                     @NonNull List<String> orgUnits,
                                                                     @Nullable HashMap<String, String> queryData);

    @NonNull
    LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesAll(@NonNull Program selectedProgram,
                                                                 @NonNull List<String> orgUnits,
                                                                 @Nullable HashMap<String, String> queryData);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam,Date enrollmentDate);

    Observable<List<OrganisationUnitModel>> getOrgUnits(@Nullable String selectedProgramUid);

    String getProgramColor(@NonNull String programUid);

    Observable<List<TrackedEntityAttribute>> trackedEntityTypeAttributes();
}
