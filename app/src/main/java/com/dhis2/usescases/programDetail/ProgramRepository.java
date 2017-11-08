package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public interface ProgramRepository {

    @NonNull
    Observable<List<ProgramTrackedEntityAttributeModel>> programAttributes(String programId);

    @NonNull
    Observable<ProgramStageModel> programStage(String programStageId);

}
