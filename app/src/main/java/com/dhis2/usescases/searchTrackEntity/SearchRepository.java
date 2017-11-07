package com.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.option.OptionSetModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public interface SearchRepository {

    @NonNull
    Observable<List<ProgramTrackedEntityAttributeModel>> programAttributes(String programId);

    Observable<List<TrackedEntityAttributeModel>> programAttributes();

    Observable<List<OptionModel>> optionSet(String optionSetId);
}
