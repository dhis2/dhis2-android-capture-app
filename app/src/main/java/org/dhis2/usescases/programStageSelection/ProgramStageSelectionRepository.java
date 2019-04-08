package org.dhis2.usescases.programStageSelection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public interface ProgramStageSelectionRepository {

   /* @NonNull
    Observable<List<ProgramStageModel>> getProgramStages(String programStages);*/

    @NonNull
    Flowable<List<ProgramStage>> enrollmentProgramStages(String programId, String enrollmentUid);

    Flowable<Result<RuleEffect>> calculate();

    List<Pair<ProgramStage, ObjectStyle>> objectStyle(List<ProgramStage> programStageModel);
}
