package org.dhis2.usescases.enrollment

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.bindings.blockingGetCheck
import org.dhis2.bindings.profilePicturePath
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.form.bindings.toRuleAttributeValue
import org.dhis2.form.data.RulesRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.TriggerEnvironment

class EnrollmentFormRepositoryImpl(
    val d2: D2,
    rulesRepository: RulesRepository,
    private val enrollmentRepository: EnrollmentObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    teiRepository: TrackedEntityInstanceObjectRepository,
    private val enrollmentService: DhisEnrollmentUtils,
) : EnrollmentFormRepository {

    private var cachedRuleEngineFlowable: Flowable<RuleEngine>
    private var ruleEnrollmentBuilder: RuleEnrollment.Builder
    private var programUid: String =
        programRepository.blockingGet()?.uid() ?: throw NullPointerException()
    private var enrollmentUid: String =
        enrollmentRepository.blockingGet()?.uid() ?: throw NullPointerException()
    private val tei: TrackedEntityInstance =
        teiRepository.blockingGet() ?: throw NullPointerException()

    init {
        this.cachedRuleEngineFlowable =
            Single.zip(
                rulesRepository.rulesNew(programUid),
                rulesRepository.ruleVariables(programUid),
                rulesRepository.enrollmentEvents(
                    enrollmentRepository.blockingGet()?.uid() ?: "",
                ),
                rulesRepository.queryConstants(),
                rulesRepository.supplementaryData(
                    enrollmentRepository.blockingGet()?.organisationUnit() ?: "",
                ),
                { rules, variables, events, constants, supplData ->
                    val builder = RuleEngineContext.builder()
                        .rules(rules)
                        .ruleVariables(variables)
                        .supplementaryData(supplData)
                        .constantsValue(constants)
                        .build().toEngineBuilder()
                    builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                    builder.events(events)
                    builder.build()
                },
            ).toFlowable()
                .cacheWithInitialCapacity(1)

        this.ruleEnrollmentBuilder = RuleEnrollment.builder()
            .enrollment(enrollmentRepository.blockingGet()?.uid())
            .incidentDate(
                if (enrollmentRepository.blockingGet()?.incidentDate() == null) {
                    enrollmentRepository.blockingGet()?.enrollmentDate()
                } else {
                    enrollmentRepository.blockingGet()?.incidentDate()
                },
            )
            .enrollmentDate(enrollmentRepository.blockingGet()?.enrollmentDate())
            .status(
                RuleEnrollment.Status.valueOf(enrollmentRepository.blockingGet()?.status()!!.name),
            )
            .organisationUnit(enrollmentRepository.blockingGet()?.organisationUnit())
            .organisationUnitCode(
                d2.organisationUnitModule().organisationUnits().uid(
                    enrollmentRepository.blockingGet()?.organisationUnit(),
                ).blockingGet()?.code(),
            )
            .programName(programRepository.blockingGet()?.displayName())
    }

    override fun ruleEngine(): Flowable<RuleEngine> {
        return cachedRuleEngineFlowable
    }

    override fun generateEvents(): Single<Pair<String, String?>> {
        return Single.fromCallable { enrollmentService.generateEnrollmentEvents(enrollmentUid) }
    }

    override fun calculate(): Flowable<Result<List<RuleEffect>>> {
        return queryAttributes()
            .map { ruleEnrollmentBuilder.attributeValues(it).build() }
            .switchMap { ruleEnrollment ->
                ruleEngine().flatMap { ruleEngine ->
                    Flowable.fromCallable(ruleEngine.evaluate(ruleEnrollment))
                }
                    .map {
                        Result.success(it)
                    }
                    .onErrorReturn {
                        Result.failure(Exception(it))
                    }
            }
    }

    private fun queryAttributes(): Flowable<List<RuleAttributeValue>> {
        return programRepository.get()
            .map { program ->
                d2.programModule().programTrackedEntityAttributes().byProgram().eq(program.uid())
                    .blockingGet()
                    .filter {
                        d2.trackedEntityModule().trackedEntityAttributeValues()
                            .value(
                                it.trackedEntityAttribute()!!.uid(),
                                enrollmentRepository.blockingGet()?.trackedEntityInstance()!!,
                            )
                            .blockingExists()
                    }.mapNotNull {
                        d2.trackedEntityModule().trackedEntityAttributeValues()
                            .value(
                                it.trackedEntityAttribute()!!.uid(),
                                enrollmentRepository.blockingGet()?.trackedEntityInstance()!!,
                            )
                            .blockingGetCheck(d2, it.trackedEntityAttribute()!!.uid())
                    }.toRuleAttributeValue(d2, program.uid())
            }.toFlowable()
    }

    override fun getProfilePicture() = tei.profilePicturePath(d2, programUid)

    override fun getProgramStageUidFromEvent(eventUi: String) =
        d2.eventModule().events().uid(eventUi).blockingGet()?.programStage()
}
