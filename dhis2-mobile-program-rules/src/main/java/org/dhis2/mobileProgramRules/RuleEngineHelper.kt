package org.dhis2.mobileProgramRules

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.rules.RuleEngineContextData
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEvent

class RuleEngineHelper(
    private val evaluationType: EvaluationType,
    private val rulesRepository: RulesRepository,
) {
    private val ruleEngine by lazy { RuleEngine.getInstance() }
    private lateinit var contextData: RuleEngineContextData
    private var refreshContext: Boolean = false
    private lateinit var targetEnrollment: RuleEnrollment
    private lateinit var targetEvent: RuleEvent

    fun evaluate(): List<RuleEffect> {
        var ruleEffects = emptyList<RuleEffect>()
        runBlocking {
            async { buildRuleEngineContextData(evaluationType.targetUid) }.await()
            ruleEffects = async {
                when (evaluationType) {
                    is EvaluationType.Enrollment ->
                        ruleEngine.evaluate(
                            target = buildTargetEnrollment(evaluationType.targetUid).copy(
                                attributeValues = rulesRepository.queryAttributeValues(
                                    evaluationType.targetUid,
                                ),
                            ),
                            ruleEvents = contextData.ruleEvents,
                            executionContext = contextData.ruleEngineContext,
                        )

                    is EvaluationType.Event ->
                        ruleEngine.evaluate(
                            target = buildTargetEvent(evaluationType.targetUid).copy(
                                dataValues = rulesRepository.queryDataValues(
                                    evaluationType.targetUid,
                                ),
                            ),
                            ruleEnrollment = contextData.ruleEnrollment,
                            ruleEvents = contextData.ruleEvents,
                            executionContext = contextData.ruleEngineContext,
                        )
                }
            }.await()
        }
        return ruleEffects
    }

    private suspend fun buildRuleEngineContextData(targetUid: String) {
        if (::contextData.isInitialized.not() || refreshContext) {
            val (programUid, orgUnitUid) = if (evaluationType is EvaluationType.Enrollment) {
                rulesRepository.enrollmentProgram(enrollmentUid = targetUid)
            } else {
                rulesRepository.eventProgram(eventUid = targetUid)
            }

            coroutineScope {
                val rules = async {
                    rulesRepository.rules(
                        programUid = programUid,
                        eventUid = if (evaluationType !is EvaluationType.Enrollment) {
                            targetUid
                        } else {
                            null
                        },
                    )
                }

                val ruleVariables = async { rulesRepository.ruleVariables(programUid = programUid) }
                val supplData = async { rulesRepository.supplementaryData(orgUnitUid = orgUnitUid) }
                val constants = async { rulesRepository.constants() }
                val ruleEnrollment = async {
                    if (evaluationType !is EvaluationType.Enrollment) {
                        rulesRepository.enrollment(
                            eventUid = targetUid,
                        )
                    } else {
                        null
                    }
                }
                val ruleEvents = async {
                    when (evaluationType) {
                        is EvaluationType.Enrollment -> rulesRepository.enrollmentEvents(
                            enrollmentUid = targetUid,
                        )

                        is EvaluationType.Event -> rulesRepository.otherEvents(
                            eventUidToEvaluate = targetUid,
                        )
                    }
                }

                contextData = RuleEngineContextData(
                    ruleEngineContext = RuleEngineContext(
                        rules = rules.await(),
                        ruleVariables = ruleVariables.await(),
                        supplementaryData = supplData.await(),
                        constantsValues = constants.await(),
                    ),
                    ruleEnrollment = ruleEnrollment.await(),
                    ruleEvents = ruleEvents.await(),
                )
            }
            refreshContext = false
        }
    }

    private fun buildTargetEnrollment(enrollmentUid: String): RuleEnrollment {
        if (::targetEnrollment.isInitialized.not()) {
            targetEnrollment = rulesRepository.getRuleEnrollment(enrollmentUid)
        }

        return targetEnrollment
    }

    private fun buildTargetEvent(eventUid: String): RuleEvent {
        if (::targetEvent.isInitialized.not()) {
            targetEvent = rulesRepository.getRuleEvent(eventUid)
        }

        return targetEvent
    }

    fun refreshContext() {
        refreshContext = true
    }
}
