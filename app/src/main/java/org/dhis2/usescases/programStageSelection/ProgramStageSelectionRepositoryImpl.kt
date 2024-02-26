package org.dhis2.usescases.programStageSelection

import io.reactivex.Flowable
import org.dhis2.commons.data.EventCreationType
import org.dhis2.mobileProgramRules.EvaluationType
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

class ProgramStageSelectionRepositoryImpl internal constructor(
    private val programUid: String,
    private val enrollmentUid: String?,
    private val eventCreationType: String,
    private val d2: D2,
) : ProgramStageSelectionRepository {
    private val ruleEngineHelper = enrollmentUid?.let {
        RuleEngineHelper(
            EvaluationType.Enrollment(enrollmentUid),
            org.dhis2.mobileProgramRules.RulesRepository(d2),
        )
    }

    override fun enrollmentProgramStages(): Flowable<List<ProgramStage>> {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid ?: "")
            .byDeleted().isFalse.get()
            .toFlowable().flatMapIterable { events: List<Event>? -> events }
            .map { event: Event -> event.programStage() }
            .toList()
            .flatMap { currentProgramStagesUids: List<String?> ->
                var repository = d2.programModule().programStages().byProgramUid().eq(
                    programUid,
                )
                if (eventCreationType == EventCreationType.SCHEDULE.name) {
                    repository =
                        repository.byHideDueDate().eq(false)
                }
                repository.get().toFlowable()
                    .flatMapIterable { stages: List<ProgramStage>? -> stages }
                    .filter { programStage: ProgramStage ->
                        programStage.access().data()
                            .write() == true && (
                            !currentProgramStagesUids.contains(programStage.uid()) ||
                                programStage.repeatable()!!
                            )
                    }
                    .toList()
            }.toFlowable()
    }

    override fun calculate(): Flowable<Result<RuleEffect>> {
        return Flowable.just(ruleEngineHelper?.evaluate() ?: emptyList())
            .map<Result<RuleEffect>> { items ->
                Result.success(items)
            }
            .onErrorReturn { Result.failure(Exception(it)) as Result<RuleEffect> }
    }

    override fun getStage(programStageUid: String): ProgramStage? {
        return d2.programModule().programStages().uid(programStageUid).blockingGet()
    }
}
