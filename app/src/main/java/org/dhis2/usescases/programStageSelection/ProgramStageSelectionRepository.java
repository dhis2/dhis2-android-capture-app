package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

import org.dhis2.utils.Result;

import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface ProgramStageSelectionRepository {

    @NonNull
    Observable<List<ProgramStageModel>> getProgramStages(String programStages);

    @NonNull
    Flowable<List<ProgramStageModel>> enrollmentProgramStages(String programId, String enrollmentUid);

    Flowable<Result<RuleEffect>> calculate();
}
