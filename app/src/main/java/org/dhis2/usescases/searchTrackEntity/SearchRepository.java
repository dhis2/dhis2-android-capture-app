package org.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(@NonNull String teType,
                                                                        @Nullable ProgramModel selectedProgram,
                                                                        @Nullable HashMap<String, String> queryData, Integer page);

    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstancesToUpdate(@NonNull String teType,
                                                                                @Nullable ProgramModel selectedProgram,
                                                                                @Nullable HashMap<String, String> queryData);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryDatam,Date enrollmentDate);

    Observable<List<OrganisationUnitModel>> getOrgUnits(@Nullable String selectedProgramUid);

    Observable<List<TrackedEntityInstance>> isOnLocalStorage(List<TrackedEntityInstance> tei);

    Flowable<List<SearchTeiModel>> transformIntoModel(List<SearchTeiModel> teiList, @Nullable ProgramModel selectedProgram);

    String getProgramColor(@NonNull String programUid);
}
