package org.dhis2.data.forms.dataentry

import io.reactivex.Flowable
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.dhis2.data.forms.FormRepository
import org.dhis2.form.bindings.toRuleAttributeValue
import org.dhis2.form.bindings.toRuleEngineObject
import org.dhis2.form.bindings.toRuleList
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import java.util.Date

class EnrollmentRuleEngineRepository(
    private val formRepository: FormRepository,
    private val enrollmentUid: String,
    private val d2: D2,
) : RuleEngineRepository {
    private val ruleEngine = RuleEngine.getInstance()
    private var ruleEnrollment: RuleEnrollment? = null
    private val attributeRules: MutableMap<String, MutableList<Rule>> = mutableMapOf()
    private var mandatoryRules: MutableList<ProgramRule> = mutableListOf()

    init {
        if (!enrollmentUid.isEmpty()) {
            initData()
        }
    }

    private fun initData() {
        val enrollment = d2.enrollmentModule().enrollments().uid(
            enrollmentUid,
        ).blockingGet()!!
        val ou = d2.organisationUnitModule().organisationUnits().uid(enrollment.organisationUnit())
            .blockingGet()!!
        val program = d2.programModule().programs().uid(enrollment.program())
            .blockingGet()!!
        ruleEnrollment = RuleEnrollment(
            enrollment.uid(),
            program.displayName()!!,
            (
                enrollment.incidentDate() ?: enrollment.enrollmentDate()
                    ?: Date()
                ).toRuleEngineLocalDate(),
            enrollment.enrollmentDate()!!.toRuleEngineLocalDate(),
            RuleEnrollment.Status.valueOf(enrollment.status()!!.name),
            enrollment.organisationUnit()!!,
            ou.code()!!,
            emptyList(),
        )
        loadAttrRules(program.uid())
    }

    private fun loadAttrRules(programUid: String) {
        val rules = d2.programModule().programRules().byProgramUid().eq(programUid)
            .withProgramRuleActions().blockingGet().toMutableList()
        mandatoryRules = mutableListOf()
        val ruleIterator = rules.iterator()
        while (ruleIterator.hasNext()) {
            val rule = ruleIterator.next()
            if (rule.condition() == null || rule.programStage() != null) {
                ruleIterator.remove()
            } else {
                for (action in rule.programRuleActions()!!) if (action.programRuleActionType() == ProgramRuleActionType.HIDEFIELD || action.programRuleActionType() == ProgramRuleActionType.HIDEPROGRAMSTAGE || action.programRuleActionType() == ProgramRuleActionType.HIDESECTION || action.programRuleActionType() == ProgramRuleActionType.ASSIGN || action.programRuleActionType() == ProgramRuleActionType.SHOWWARNING || action.programRuleActionType() == ProgramRuleActionType.SHOWERROR || action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR || action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTIONGROUP || action.programRuleActionType() == ProgramRuleActionType.HIDEOPTION || action.programRuleActionType() == ProgramRuleActionType.SHOWOPTIONGROUP || action.programRuleActionType() == ProgramRuleActionType.SETMANDATORYFIELD) if (!mandatoryRules.contains(
                        rule,
                    )
                ) {
                    mandatoryRules.add(rule)
                }
            }
        }
        val variables = d2.programModule().programRuleVariables().byProgramUid().eq(programUid)
            .blockingGet().toMutableList()
        val variableIterator = variables.iterator()
        while (variableIterator.hasNext()) {
            val variable = variableIterator.next()
            if (variable.trackedEntityAttribute() == null) variableIterator.remove()
        }
        val finalMandatoryRules = mandatoryRules.toRuleList().toMutableList()
        for (variable in variables) {
            if (variable.trackedEntityAttribute() != null &&
                !attributeRules.containsKey(variable.trackedEntityAttribute()!!.uid())
            ) {
                attributeRules[variable.trackedEntityAttribute()!!.uid()] = finalMandatoryRules
            }
            for (rule in rules) {
                if (rule.condition()!!.contains(variable.displayName()!!) ||
                    actionsContainsAttr(rule.programRuleActions(), variable.displayName())
                ) {
                    if (attributeRules[
                            variable.trackedEntityAttribute()!!
                                .uid(),
                        ] == null
                    ) {
                        attributeRules[variable.trackedEntityAttribute()!!.uid()] =
                            finalMandatoryRules
                    }
                    attributeRules[
                        variable.trackedEntityAttribute()!!
                            .uid(),
                    ]?.add(rule.toRuleEngineObject())
                }
            }
        }
    }

    private fun actionsContainsAttr(
        programRuleActions: List<ProgramRuleAction>?,
        variableName: String?,
    ): Boolean {
        var actionContainsDe = false
        for (ruleAction in programRuleActions!!) {
            if (ruleAction.data() != null && ruleAction.data()!!
                    .contains(variableName!!)
            ) {
                actionContainsDe = true
            }
        }
        return actionContainsDe
    }

    override fun updateRuleEngine(): Flowable<RuleEngineContextData> {
        return formRepository.restartRuleEngine()
    }

    override fun calculate(): Flowable<Result<RuleEffect>> {
        return queryAttributeValues()
            .map { ruleAttributeValues ->
                ruleEnrollment?.copy(attributeValues = ruleAttributeValues)
            }
            .switchMap { enrollment ->
                formRepository.ruleEngine()
                    .switchMap { ruleEngineData ->
                        Flowable.just(
                            ruleEngine.evaluate(
                                target = enrollment,
                                ruleEvents = ruleEngineData.ruleEvents,
                                executionContext = ruleEngineData.ruleEngineContext,
                            ),
                        )
                    }
                    .map { items -> Result.success(items) }
                    .onErrorReturn { error: Throwable? -> Result.failure(Exception(error)) as Result<RuleEffect> }
            }
    }

    override fun reCalculate(): Flowable<Result<RuleEffect>> {
        initData()
        return calculate()
    }

    private fun queryAttributeValues(): Flowable<List<RuleAttributeValue>> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .flatMap { enrollment ->
                d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).get()
                    .map { list ->
                        list.toRuleAttributeValue(
                            d2,
                            enrollment.program()!!,
                        )
                    }
            }.toFlowable()
    }
}
