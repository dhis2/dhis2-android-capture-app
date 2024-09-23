package org.dhis2.data.forms

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.form.data.RulesRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.rules.api.RuleEngineContext

class EnrollmentFormRepository(
    private val rulesRepository: RulesRepository,
    private val enrollmentUid: String,
    private val d2: D2,
) : FormRepository {
    private var cachedRuleEngineFlowable: Flowable<RuleEngineContextData>
    private var enrollmentOrgUnitUid: String? = null

    init {
        enrollmentOrgUnitUid = if (enrollmentUid.isNotEmpty()) {
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()!!
                .organisationUnit()
        } else {
            ""
        }
        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        cachedRuleEngineFlowable = enrollmentProgram()
            .switchMap { program ->
                Single.zip(
                    rulesRepository.rulesNew(program, null).subscribeOn(Schedulers.io()),
                    rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                    rulesRepository.enrollmentEvents(enrollmentUid).subscribeOn(Schedulers.io()),
                    rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                    rulesRepository.supplementaryData(enrollmentOrgUnitUid!!)
                        .subscribeOn(Schedulers.io()),
                ) { rules, variables, events, constants, supplementaryData ->

                    val ruleEngineContext = RuleEngineContext(
                        rules,
                        variables,
                        supplementaryData,
                        constants,
                    )
                    RuleEngineContextData(
                        ruleEngineContext = ruleEngineContext,
                        ruleEnrollment = null,
                        ruleEvents = events,
                    )
                }.toFlowable()
            }
            .cacheWithInitialCapacity(1)
    }

    override fun restartRuleEngine(): Flowable<RuleEngineContextData> {
        val orgUnit = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()!!
            .organisationUnit()
        return enrollmentProgram()
            .switchMap { program ->
                Single.zip(
                    rulesRepository.rulesNew(program, null),
                    rulesRepository.ruleVariables(program),
                    rulesRepository.enrollmentEvents(enrollmentUid),
                    rulesRepository.queryConstants(),
                    rulesRepository.supplementaryData(orgUnit!!),
                ) { rules, variables, events, constants, supplementaryData ->
                    val ruleEngineContext = RuleEngineContext(
                        rules,
                        variables,
                        supplementaryData,
                        constants,
                    )
                    RuleEngineContextData(
                        ruleEngineContext = ruleEngineContext,
                        ruleEnrollment = null,
                        ruleEvents = events,
                    )
                }.toFlowable()
            }
            .cacheWithInitialCapacity(1).also { cachedRuleEngineFlowable = it }
    }

    override fun ruleEngine(): Flowable<RuleEngineContextData> {
        return cachedRuleEngineFlowable
    }

    private fun enrollmentProgram(): Flowable<String> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .map { it.program()!! }
            .toFlowable()
    }
}
