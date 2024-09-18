package org.dhis2.android.rtsm.services.rules

import io.reactivex.Flowable
import io.reactivex.Single
import org.apache.commons.lang3.math.NumberUtils
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.utils.ConfigUtils
import org.dhis2.android.rtsm.utils.RuleEngineHelper
import org.dhis2.android.rtsm.utils.printRuleEffects
import org.dhis2.android.rtsm.utils.toRuleDataValue
import org.dhis2.android.rtsm.utils.toRuleList
import org.dhis2.android.rtsm.utils.toRuleVariableList
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.commons.rules.toRuleEngineInstant
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleEventStatus
import org.hisp.dhis.rules.models.RuleVariable
import timber.log.Timber
import java.util.Date
import java.util.Objects
import java.util.UUID
import javax.inject.Inject

class RuleValidationHelperImpl @Inject constructor(
    private val d2: D2,
) : RuleValidationHelper {

    private val ruleEngine = RuleEngine.getInstance()

    override fun evaluate(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String?,
        appConfig: AppConfig,
    ): Flowable<List<RuleEffect>> {
        return ruleEngineData(entry.item.id, appConfig.program).flatMap { ruleEngineData ->
            val programStage = programStage(program) ?: return@flatMap Flowable.empty()

            Flowable.just(
                prepareForDataEntry(ruleEngineData, programStage, transaction, entry.date),
            ).flatMap { prelimRuleEffects ->
                val dataValues = mutableListOf<RuleDataValue>().apply {
                    addAll(
                        entryDataValues(
                            entry.qty,
                            programStage.uid(),
                            transaction,
                            entry.date,
                            appConfig,
                        ),
                    )
                }

                prelimRuleEffects.forEach { ruleEffect ->
                    when (ruleEffect.ruleAction.type) {
                        ProgramRuleActionType.ASSIGN.name -> {
                            val ruleAction = ruleEffect.ruleAction
                            ruleEffect.data?.let { data ->
                                dataValues.add(
                                    RuleDataValue(
                                        entry.date.toRuleEngineInstant(),
                                        programStage.uid(),
                                        ruleAction.field()!!,
                                        data,
                                    ),
                                )
                            }
                        }
                    }
                }

                printRuleEffects("Preliminary RuleEffects", prelimRuleEffects, dataValues)

                Flowable.just(
                    ruleEngine.evaluate(
                        target = createRuleEvent(
                            programStage,
                            transaction.facility.uid,
                            dataValues,
                            entry.date,
                            null,
                        ),
                        ruleEnrollment = ruleEngineData.ruleEnrollment,
                        ruleEvents = ruleEngineData.ruleEvents,
                        executionContext = ruleEngineData.ruleEngineContext,
                    ),
                )
            }
        }
    }

    /**
     * Evaluate the program rules on a blank new rule event in preparation for
     * data entry
     */
    private fun prepareForDataEntry(
        ruleEngineData: RuleEngineContextData,
        programStage: ProgramStage,
        transaction: Transaction,
        eventDate: Date,
    ) = ruleEngine.evaluate(
        target = createRuleEvent(programStage, transaction.facility.uid, listOf(), eventDate),
        ruleEnrollment = ruleEngineData.ruleEnrollment,
        ruleEvents = ruleEngineData.ruleEvents,
        executionContext = ruleEngineData.ruleEngineContext,
    )

    private fun createRuleEvent(
        programStage: ProgramStage,
        organisationUnit: String,
        dataValues: List<RuleDataValue>,
        period: Date,
        eventUid: String? = null,
    ) = RuleEvent(
        eventUid ?: UUID.randomUUID().toString(),
        programStage.uid(),
        programStage.name()!!,
        RuleEventStatus.ACTIVE,
        period.toRuleEngineInstant(),
        period.toRuleEngineLocalDate(),
        period.toRuleEngineLocalDate(),
        organisationUnit,
        null,
        dataValues,
    )

    private fun programStage(programUid: String) = d2.programModule().programStages()
        .byProgramUid().eq(programUid)
        .one().blockingGet()

    private fun ruleVariables(programUid: String): Single<List<RuleVariable>> {
        return d2.programModule().programRuleVariables()
            .byProgramUid().eq(programUid)
            .get()
            .map {
                it.toRuleVariableList(
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                    d2.optionModule().options(),
                )
            }
    }

    private fun constants(): Single<Map<String, String>> {
        return d2.constantModule().constants().get()
            .map { constants ->
                val constantsMap = HashMap<String, String>()
                for (constant in constants) {
                    constantsMap[constant.uid()] =
                        Objects.requireNonNull<Double>(constant.value()).toString()
                }
                constantsMap
            }
    }

    private fun supplementaryData(): Single<Map<String, List<String>>> = Single.just(hashMapOf())

    private fun ruleEngineData(teiUid: String, programUid: String): Flowable<RuleEngineContextData> {
        val enrollment = currentEnrollment(teiUid, programUid)

        val enrollmentEvents = if (enrollment == null) {
            Single.just(listOf())
        } else {
            enrollmentEvents(enrollment)
        }

        return Single.zip(
            programRules(programUid),
            ruleVariables(programUid),
            constants(),
            supplementaryData(),
            enrollmentEvents,
        ) { rules, variables, constants, supplData, events ->
            RuleEngineHelper.getRuleEngine(rules, variables, constants, supplData, events)
        }
            .toFlowable()
            .cacheWithInitialCapacity(1)
    }

    private fun currentEnrollment(teiUid: String, programUid: String): Enrollment? {
        val enrollments = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)
            .byProgram().eq(programUid)
            .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
            .blockingGet()

        var mostRecentEnrollment: Enrollment? = null
        for (enrollment in enrollments) {
            if (enrollment.status() == EnrollmentStatus.ACTIVE) {
                mostRecentEnrollment = enrollment
                break
            }
        }

        if (mostRecentEnrollment == null && enrollments.isNotEmpty()) {
            mostRecentEnrollment = enrollments[0]
        }

        Timber.d("Enrollment: %s", mostRecentEnrollment)

        return mostRecentEnrollment
    }

    private fun enrollmentEvents(enrollment: Enrollment): Single<List<RuleEvent>>? {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid())
            .byStatus().notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
            .byEventDate().beforeOrEqual(Date())
            .orderByCreated(RepositoryScope.OrderByDirection.DESC)
            .withTrackedEntityDataValues()
            .get()
            .toFlowable().flatMapIterable { events -> events }
            .map { event ->
                RuleEvent(
                    event.uid(),
                    event.programStage()!!,
                    d2.programModule().programStages().uid(event.programStage())
                        .blockingGet()!!.name()!!,
                    if (event.status() == EventStatus.VISITED) {
                        RuleEventStatus.ACTIVE
                    } else {
                        RuleEventStatus.valueOf(event.status()!!.name)
                    },
                    (event.eventDate() ?: Date()).toRuleEngineInstant(),
                    event.dueDate()?.toRuleEngineLocalDate(),
                    event.completedDate()?.toRuleEngineLocalDate(),
                    event.organisationUnit()!!,
                    d2.organisationUnitModule()
                        .organisationUnits().uid(event.organisationUnit())
                        .blockingGet()?.code(),
                    event.trackedEntityDataValues()?.toRuleDataValue(
                        event,
                        d2.dataElementModule().dataElements(),
                        d2.programModule().programRuleVariables(),
                        d2.optionModule().options(),
                    ) ?: emptyList(),
                )
            }.toList()
    }

    private fun programRules(programUid: String, eventUid: String? = null) =
        d2.programModule().programRules()
            .byProgramUid().eq(programUid)
            .withProgramRuleActions()
            .get()
            .map { it.toRuleList() }
            .map {
                if (eventUid != null) {
                    val programStage = d2.eventModule().events()
                        .uid(eventUid).blockingGet()?.programStage()
                    it.filter { rule ->
                        rule.programStage == null || rule.programStage == programStage
                    }
                } else {
                    it
                }
            }

    private fun entryDataValues(
        qty: String?,
        programStage: String,
        transaction: Transaction,
        eventDate: Date,
        appConfig: AppConfig,
    ): List<RuleDataValue> {
        val values = mutableListOf<RuleDataValue>()

        // Add the quantity if defined, and valid (signs (+/-) could come as streams if incomplete)
        if (qty != null && NumberUtils.isCreatable(qty)) {
            val deUid =
                ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
            values.add(
                RuleDataValue(
                    eventDate = eventDate.toRuleEngineInstant(),
                    programStage = programStage,
                    dataElement = deUid,
                    value = qty,
                ),
            )

            // Add the 'deliver to' if it's a distribution event
            if (transaction.transactionType == TransactionType.DISTRIBUTION) {
                transaction.distributedTo?.let { distributedTo ->
                    d2.optionModule()
                        .options()
                        .uid(distributedTo.uid)
                        .blockingGet()
                        ?.code()?.let { code ->
                            values.add(
                                RuleDataValue(
                                    eventDate = eventDate.toRuleEngineInstant(),
                                    programStage = programStage,
                                    dataElement = appConfig.distributedTo,
                                    value = code,
                                ),
                            )
                        }
                }
            }
        }

        return values.toList()
    }
}
