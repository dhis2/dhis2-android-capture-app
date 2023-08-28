package org.dhis2.form.data

import org.dhis2.form.bindings.toRuleDataValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.TriggerEnvironment

class EventRuleEngineRepository(
    private val d2: D2,
    private val eventUid: String,
) : RuleEngineRepository {

    private val ruleRepository = RulesRepository(d2)
    private val ruleEngine: RuleEngine
    private val eventBuilder: RuleEvent.Builder

    private val event: Event by lazy {
        d2.eventModule().events()
            .uid(eventUid)
            .blockingGet() ?: throw NullPointerException()
    }

    private val program: Program by lazy {
        d2.programModule().programs()
            .uid(event.program())
            .blockingGet() ?: throw NullPointerException()
    }

    init {

        val rules = ruleRepository.rulesNew(program.uid(), eventUid).blockingGet()
        val variables = ruleRepository.ruleVariables(program.uid()).blockingGet()
        val supplData = ruleRepository.supplementaryData(event.organisationUnit()!!).blockingGet()
        val constants = ruleRepository.queryConstants().blockingGet()
        val events = ruleRepository.otherEvents(eventUid).blockingGet()
        val ruleEnrollment = ruleRepository.enrollment(eventUid).blockingGet()

        ruleEngine = RuleEngineContext.builder()
            .rules(rules)
            .ruleVariables(variables)
            .supplementaryData(supplData)
            .constantsValue(constants)
            .build().toEngineBuilder().apply {
                triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                events(events)
                if (ruleEnrollment.enrollment().isNotEmpty()) {
                    enrollment(ruleEnrollment)
                }
            }.build()

        eventBuilder = RuleEvent.builder()
        val currentStage = d2.programModule().programStages()
            .uid(event.programStage())
            .blockingGet()
        val ou = d2.organisationUnitModule().organisationUnits()
            .uid(event.organisationUnit())
            .blockingGet()
        eventBuilder
            .event(eventUid)
            .programStage(event.programStage())
            .programStageName(currentStage?.displayName())
            .status(RuleEvent.Status.valueOf(event.status()!!.name))
            .eventDate(event.eventDate())
            .dueDate(if (event.dueDate() != null) event.dueDate() else event.eventDate())
            .organisationUnit(event.organisationUnit())
            .organisationUnitCode(ou?.code())
    }

    override fun calculate(): List<RuleEffect> {
        val dataElements = queryDataValues()
        eventBuilder.dataValues(dataElements)
        return try {
            ruleEngine.evaluate(eventBuilder.build()).call()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun queryDataValues(): List<RuleDataValue> {
        return d2.trackedEntityModule().trackedEntityDataValues()
            .byEvent().eq(eventUid)
            .byValue().isNotNull
            .blockingGet().toRuleDataValue(
                event,
                d2.dataElementModule().dataElements(),
                d2.programModule().programRuleVariables(),
                d2.optionModule().options(),
            )
    }
}
