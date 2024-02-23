package org.dhis2.form.data

import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.commons.rules.toRuleEngineInstant
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.dhis2.form.bindings.toRuleDataValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import java.util.Date

class EventRuleEngineRepository(
    private val d2: D2,
    private val eventUid: String,
) : RuleEngineRepository {

    private val ruleRepository = RulesRepository(d2)
    private val ruleEngineData: RuleEngineContextData
    private val ruleEvent: RuleEvent

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

        val ruleEngineContext = RuleEngineContext(
            rules = rules,
            ruleVariables = variables,
            supplementaryData = supplData,
            constantsValues = constants,
        )

        ruleEngineData = RuleEngineContextData(
            ruleEngineContext = ruleEngineContext,
            ruleEnrollment = ruleEnrollment,
            ruleEvents = events,
        )

        val currentStage = d2.programModule().programStages()
            .uid(event.programStage())
            .blockingGet()
        val ou = d2.organisationUnitModule().organisationUnits()
            .uid(event.organisationUnit())
            .blockingGet()

        ruleEvent = RuleEvent(
            eventUid,
            event.programStage()!!,
            currentStage?.displayName()!!,
            RuleEvent.Status.valueOf(event.status()!!.name),
            event.eventDate()?.toRuleEngineInstant() ?: Date().toRuleEngineInstant(),
            event.dueDate()?.toRuleEngineLocalDate(),
            event.completedDate()?.toRuleEngineLocalDate(),
            ou?.uid()!!,
            ou.code()!!,
            listOf(),
        )
    }

    override fun calculate(): List<RuleEffect> {
        val dataElements = queryDataValues()

        return try {
            RuleEngine.getInstance().evaluate(
                ruleEvent.copy(dataValues = dataElements),
                ruleEngineData.ruleEnrollment,
                ruleEngineData.ruleEvents,
                ruleEngineData.ruleEngineContext,
            )
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
