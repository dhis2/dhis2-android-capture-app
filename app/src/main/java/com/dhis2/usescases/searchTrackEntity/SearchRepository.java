package com.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public interface SearchRepository {

    @NonNull
    Observable<List<TrackedEntityAttributeModel>> programAttributes(String programId);

    Observable<List<TrackedEntityAttributeModel>> programAttributes();

    Observable<List<OptionModel>> optionSet(String optionSetId);

    Observable<List<ProgramModel>> programsWithRegistration(String programTypeId);

    Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(@NonNull String teType,
                                                                        @Nullable String programUid,
                                                                        @Nullable String enrollmentDate,
                                                                        @Nullable String incidentDate,
                                                                        @Nullable HashMap<Long, String> queryData);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnitUID, @NonNull String programUid);
}
