package org.dhis2.usescases.programStageSelection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function5
import java.util.HashMap
import org.dhis2.Bindings.toRuleAttributeValue
import org.dhis2.data.forms.RulesRepository
import org.dhis2.utils.EventCreationType
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.RuleExpressionEvaluator
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.TriggerEnvironment

class ProgramStageSelectionRepositoryImpl internal constructor(
    evaluator: RuleExpressionEvaluator,
    rulesRepository: RulesRepository,
    private val programUid: String,
    private val enrollmentUid: String,
    private val eventCreationType: String,
    private val d2: D2
) : ProgramStageSelectionRepository {

    private val cachedRuleEngineFlowable: Flowable<RuleEngine>

    @VisibleForTesting
    fun ruleEnrollment(): Flowable<RuleEnrollment> =
        d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .flatMap { enrollment: Enrollment ->
                d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(enrollment.program())
                    .get()
                    .map { programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute> ->
                        programTrackedEntityAttributes.map { it.trackedEntityAttribute()?.uid() }
                    }
                    .flatMap { attributes: List<String?> ->
                        d2.trackedEntityModule().trackedEntityAttributeValues()
                            .byTrackedEntityInstance()
                            .eq(enrollment.trackedEntityInstance())
                            .byTrackedEntityAttribute().`in`(attributes)
                            .get()
                    }
                    .map { attributeValues: List<TrackedEntityAttributeValue> ->
                        RuleEnrollment.create(
                            enrollment.uid(),
                            enrollment.incidentDate() ?: enrollment.enrollmentDate()!!,
                            enrollment.enrollmentDate()!!,
                            RuleEnrollment.Status.valueOf(
                                enrollment.status()!!.name
                            ),
                            enrollment.organisationUnit()!!,
                            d2.organisationUnitModule().organisationUnits()
                                .uid(enrollment.organisationUnit())
                                .blockingGet().code() ?: "",
                            attributeValues.toRuleAttributeValue(d2, enrollment.program()!!),
                            d2.programModule().programs()
                                .uid(enrollment.program())
                                .blockingGet().name()
                        )
                    }
            }.toFlowable()

    override fun enrollmentProgramStages(): Flowable<List<ProgramStage>> =
        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid).byDeleted().isFalse
            .get().toFlowable()
            .flatMapIterable { events -> events }
            .map { event: Event -> event.programStage() }
            .toList()
            .flatMap { currentProgramStagesUids: List<String?> ->
                var repository = d2.programModule().programStages().byProgramUid().eq(programUid)
                if (eventCreationType == EventCreationType.SCHEDULE.name) {
                    repository = repository.byHideDueDate().eq(false)
                }
                repository.get().toFlowable()
                    .flatMapIterable { stages: List<ProgramStage>? -> stages }
                    .filter { programStage: ProgramStage ->
                        !currentProgramStagesUids.contains(programStage.uid()) ||
                            programStage.repeatable()!!
                    }
                    .toList()
            }.toFlowable()

    override fun calculate(): Flowable<Result<RuleEffect>> =
        ruleEnrollment()
            .flatMap { enrollment: RuleEnrollment ->
                cachedRuleEngineFlowable
                    .switchMap { ruleEngine: RuleEngine ->
                        Flowable.fromCallable(ruleEngine.evaluate(enrollment))
                            .map { items -> Result.success(items) }
                            .onErrorReturn { error ->
                                Result.failure(Exception(error)) as Result<RuleEffect>
                            }
                    }
            }

    override fun getStage(programStageUid: String): ProgramStage =
        d2.programModule().programStages().uid(programStageUid).blockingGet()

    init {
        val orgUnitUid = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            .organisationUnit().toString()
        cachedRuleEngineFlowable =
            Single.zip(
                rulesRepository.rulesNew(programUid, null),
                rulesRepository.ruleVariablesProgramStages(programUid),
                rulesRepository.enrollmentEvents(enrollmentUid),
                rulesRepository.supplementaryData(orgUnitUid),
                rulesRepository.queryConstants(),
                Function5 { rules: List<Rule>,
                    variables: List<RuleVariable>,
                    ruleEvents: List<RuleEvent>,
                    supplementaryData: Map<String, List<String>>,
                    constants: Map<String, String> ->
                    val builder = RuleEngineContext.builder(evaluator)
                        .rules(rules)
                        .ruleVariables(variables)
                        .calculatedValueMap(HashMap())
                        .constantsValue(constants)
                        .supplementaryData(supplementaryData)
                        .build().toEngineBuilder()
                    builder.events(ruleEvents)
                        .triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                        .build()
                }
            ).toFlowable()
                .cacheWithInitialCapacity(1)
    }
}
