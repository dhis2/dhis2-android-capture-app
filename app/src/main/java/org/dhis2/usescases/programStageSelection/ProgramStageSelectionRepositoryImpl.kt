package org.dhis2.usescases.programStageSelection

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.dhis2.form.bindings.toRuleAttributeValue
import org.dhis2.form.data.RulesRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */
class ProgramStageSelectionRepositoryImpl internal constructor(
    rulesRepository: RulesRepository,
    private val programUid: String,
    private val enrollmentUid: String?,
    private val eventCreationType: String,
    private val d2: D2,
) : ProgramStageSelectionRepository {
    private val ruleEngine: RuleEngine = RuleEngine.getInstance()
    private val cachedRuleEngineFlowable: Flowable<RuleEngineContextData>

    init {
        val orgUnitUid =
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()?.organisationUnit()
        cachedRuleEngineFlowable = Single.zip(
            rulesRepository.rulesNew(programUid, null),
            rulesRepository.ruleVariablesProgramStages(programUid),
            rulesRepository.enrollmentEvents(enrollmentUid!!),
            rulesRepository.supplementaryData(orgUnitUid!!),
            rulesRepository.queryConstants(),
        ) { rules, variables, ruleEvents, supplementaryData, constants,
            ->
            val ruleEngineContext = RuleEngineContext(
                rules = rules,
                ruleVariables = variables,
                supplementaryData = supplementaryData,
                constantsValues = constants,
            )
            RuleEngineContextData(
                ruleEngineContext,
                null,
                ruleEvents,
            )
        }.toFlowable()
            .cacheWithInitialCapacity(1)
    }

    private fun ruleEnrollment(enrollmentUid: String?): Flowable<RuleEnrollment> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .flatMap { enrollment: Enrollment ->
                d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(enrollment.program())
                    .get()
                    .map<List<String>> { programTrackedEntityAttributes ->
                        val attibuteUids: MutableList<String> = ArrayList()
                        for (programTrackedEntityAttribute in programTrackedEntityAttributes) {
                            attibuteUids.add(
                                programTrackedEntityAttribute.trackedEntityAttribute()!!.uid(),
                            )
                        }
                        attibuteUids
                    }
                    .flatMap { attributes: List<String> ->
                        d2.trackedEntityModule().trackedEntityAttributeValues()
                            .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance())
                            .byTrackedEntityAttribute().`in`(attributes)
                            .get()
                    }
                    .map { attributeValues: List<TrackedEntityAttributeValue> ->
                        RuleEnrollment(
                            enrollment.uid(),
                            d2.programModule().programs().uid(enrollment.program()).blockingGet()
                                ?.name()!!,
                            if (enrollment.incidentDate() == null) {
                                enrollment.enrollmentDate()!!
                            } else {
                                enrollment.incidentDate()!!
                            }.toRuleEngineLocalDate(),
                            enrollment.enrollmentDate()!!.toRuleEngineLocalDate(),
                            RuleEnrollment.Status.valueOf(enrollment.status()!!.name),
                            enrollment.organisationUnit()!!,
                            getOrgUnitCode(enrollment.organisationUnit()),
                            attributeValues.toRuleAttributeValue(d2, enrollment.program()!!),
                        )
                    }
            }.toFlowable()
    }

    private fun getOrgUnitCode(orgUnitUid: String?): String {
        return d2.organisationUnitModule().organisationUnits()
            .uid(orgUnitUid).blockingGet()?.code() ?: ""
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
        return ruleEnrollment(enrollmentUid)
            .flatMap { enrollment ->
                cachedRuleEngineFlowable
                    .switchMap { ruleEngineData ->
                        Flowable.just(
                            ruleEngine.evaluate(
                                target = enrollment,
                                ruleEngineData.ruleEvents,
                                ruleEngineData.ruleEngineContext,
                            ),
                        )
                            .map<Result<RuleEffect>> { items ->
                                Result.success(items)
                            }
                            .onErrorReturn { Result.failure(Exception(it)) as Result<RuleEffect> }
                    }
            }
    }

    override fun getStage(programStageUid: String): ProgramStage? {
        return d2.programModule().programStages().uid(programStageUid).blockingGet()
    }
}
