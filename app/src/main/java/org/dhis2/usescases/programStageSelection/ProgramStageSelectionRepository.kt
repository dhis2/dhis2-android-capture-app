package org.dhis2.usescases.programStageSelection

import io.reactivex.Flowable
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

interface ProgramStageSelectionRepository {
    fun enrollmentProgramStages(): Flowable<List<ProgramStage>>
    fun calculate(): Flowable<Result<RuleEffect>>
    fun getStage(programStageUid: String): ProgramStage?
}
