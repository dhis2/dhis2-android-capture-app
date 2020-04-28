package org.dhis2.usescases.programStageSelection;

import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface ProgramStageSelectionRepository {
    @NonNull
    Flowable<List<ProgramStage>> enrollmentProgramStages();

    Flowable<Result<RuleEffect>> calculate();

    ProgramStage getStage(String programStageUid);
}
