package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import io.reactivex.Flowable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.data.forms.FormRepository
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.form.bindings.toRuleDataValue
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent

class EventRuleEngineRepository(
    var d2: D2,
    var formRepository: FormRepository,
    var eventUid: String?,
) : RuleEngineRepository {
    private var ruleEvent: RuleEvent? = null

    init {
        initData()
    }

    private fun initData() {
        eventUid?.let {
            val currentEvent = d2.eventModule().events()
                .withTrackedEntityDataValues()
                .uid(eventUid)
                .blockingGet()!!
            val currentStage = d2.programModule().programStages()
                .uid(currentEvent.programStage())
                .blockingGet()
            val ou = d2.organisationUnitModule().organisationUnits()
                .uid(currentEvent.organisationUnit())
                .blockingGet()

            ruleEvent = RuleEvent(
                currentEvent.uid(),
                currentEvent.programStage()!!,
                currentStage!!.displayName()!!,
                when (currentEvent.status()) {
                    EventStatus.ACTIVE -> RuleEvent.Status.ACTIVE
                    EventStatus.COMPLETED -> RuleEvent.Status.COMPLETED
                    EventStatus.SCHEDULE -> RuleEvent.Status.SCHEDULE
                    EventStatus.SKIPPED -> RuleEvent.Status.SKIPPED
                    EventStatus.VISITED -> RuleEvent.Status.VISITED
                    EventStatus.OVERDUE -> RuleEvent.Status.OVERDUE
                    else -> RuleEvent.Status.ACTIVE
                },
                Instant.fromEpochMilliseconds(currentEvent.eventDate()!!.time),
                Instant.fromEpochMilliseconds(
                    if (currentEvent.dueDate() != null) {
                        currentEvent.dueDate()!!.time
                    } else {
                        currentEvent.eventDate()!!.time
                    },
                ).toLocalDateTime(TimeZone.currentSystemDefault()).date,
                Instant.fromEpochMilliseconds(
                    currentEvent.completedDate()!!.time,
                ).toLocalDateTime(TimeZone.currentSystemDefault()).date,
                currentEvent.organisationUnit()!!,
                ou!!.code(),
                emptyList(),
            )
        }
    }

    override fun updateRuleEngine(): Flowable<RuleEngineContextData> {
        return formRepository.restartRuleEngine()
    }

    override fun calculate(): Flowable<Result<RuleEffect>> {
        if (eventUid == null) return Flowable.just(Result.success(emptyList()))
        return queryDataValues(eventUid!!)
            .switchMap { dataValues ->
                formRepository.ruleEngine()
                    .map { ruleEngineContextData ->
                        val ruleEffects = RuleEngine.getInstance().evaluate(
                            target = ruleEvent!!.copy(dataValues = dataValues),
                            ruleEnrollment = ruleEngineContextData.ruleEnrollment,
                            ruleEngineContextData.ruleEvents,
                            ruleEngineContextData.ruleEngineContext,
                        )
                        Result.success(ruleEffects)
                    }
                    .onErrorReturn { error ->
                        Result.failure(Exception(error)) as Result<RuleEffect>
                    }
            }
    }

    override fun reCalculate(): Flowable<Result<RuleEffect>> {
        initData()
        return calculate()
    }

    private fun queryDataValues(eventUid: String): Flowable<List<RuleDataValue>> {
        return d2.eventModule().events().uid(eventUid).get()
            .flatMap { event ->
                d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)
                    .byValue().isNotNull.get()
                    .map { values ->
                        values.toRuleDataValue(
                            event,
                            d2.dataElementModule().dataElements(),
                            d2.programModule().programRuleVariables(),
                            d2.optionModule().options(),
                        )
                    }
            }.toFlowable()
    }
}
