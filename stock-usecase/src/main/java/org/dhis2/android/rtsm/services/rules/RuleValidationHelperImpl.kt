package org.dhis2.android.rtsm.services.rules

import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.commons.lang3.math.NumberUtils
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.utils.ConfigUtils
import org.dhis2.android.rtsm.utils.RuleEngineHelper
import org.dhis2.android.rtsm.utils.printRuleEffects
import org.dhis2.commons.bindings.distributedTo
import org.dhis2.commons.rules.RuleEngineContextData
import org.dhis2.mobileProgramRules.toRuleDataValue
import org.dhis2.mobileProgramRules.toRuleEngineInstant
import org.dhis2.mobileProgramRules.toRuleEngineLocalDate
import org.dhis2.mobileProgramRules.toRuleList
import org.dhis2.mobileProgramRules.toRuleVariableList
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
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

class RuleValidationHelperImpl(
    private val d2: D2,
) : RuleValidationHelper {
    private val ruleEngine = RuleEngine.getInstance()

    override fun evaluate(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String?,
        stockUseCase: StockUseCase,
    ): Flowable<List<RuleEffect>> {
        return ruleEngineData(entry.item.id, stockUseCase.programUid).flatMap { ruleEngineData ->
            val programStage = programStage(program) ?: return@flatMap Flowable.empty()

            Flowable
                .just(
                    prepareForDataEntry(ruleEngineData, programStage, transaction, entry.date),
                ).flatMap { prelimRuleEffects ->
                    val dataValues =
                        mutableListOf<RuleDataValue>().apply {
                            addAll(
                                entryDataValues(
                                    entry.qty,
                                    transaction,
                                    stockUseCase,
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
                            target =
                                createRuleEvent(
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
        period.toRuleEngineInstant(),
        period.toRuleEngineLocalDate(),
        period.toRuleEngineLocalDate(),
        organisationUnit,
        null,
        dataValues,
    )

    private fun programStage(programUid: String) =
        d2
            .programModule()
            .programStages()
            .byProgramUid()
            .eq(programUid)
            .one()
            .blockingGet()

    private fun ruleVariables(programUid: String): Single<List<RuleVariable>> =
        d2
            .programModule()
            .programRuleVariables()
            .byProgramUid()
            .eq(programUid)
            .get()
            .map {
                it.toRuleVariableList(
                    d2.optionModule().options(),
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                )
            }

    private fun constants(): Single<Map<String, String>> =
        d2
            .constantModule()
            .constants()
            .get()
            .map { constants ->
                val constantsMap = HashMap<String, String>()
                for (constant in constants) {
                    constantsMap[constant.uid()] =
                        Objects.requireNonNull<Double>(constant.value()).toString()
                }
                constantsMap
            }

    private fun supplementaryData(): Single<Map<String, List<String>>> = Single.just(hashMapOf())

    private fun ruleEngineData(
        teiUid: String,
        programUid: String,
    ): Flowable<RuleEngineContextData> {
        val enrollment = currentEnrollment(teiUid, programUid)

        val enrollmentEvents =
            if (enrollment == null) {
                Single.just(listOf())
            } else {
                enrollmentEvents(enrollment)
            }

        return Single
            .zip(
                programRules(programUid),
                ruleVariables(programUid),
                constants(),
                supplementaryData(),
                enrollmentEvents,
            ) { rules, variables, constants, supplData, events ->
                RuleEngineHelper.getRuleEngine(rules, variables, constants, supplData, events)
            }.toFlowable()
            .cacheWithInitialCapacity(1)
    }

    private fun currentEnrollment(
        teiUid: String,
        programUid: String,
    ): Enrollment? {
        val enrollments =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(teiUid)
                .byStatus()
                .eq(EnrollmentStatus.ACTIVE)
                .byProgram()
                .eq(programUid)
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

    private fun enrollmentEvents(enrollment: Enrollment): Single<List<RuleEvent>>? =
        d2
            .eventModule()
            .events()
            .byEnrollmentUid()
            .eq(enrollment.uid())
            .byStatus()
            .notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE)
            .byEventDate()
            .beforeOrEqual(Date())
            .orderByCreated(RepositoryScope.OrderByDirection.DESC)
            .withTrackedEntityDataValues()
            .get()
            .toFlowable()
            .flatMapIterable { events -> events }
            .map { event ->
                RuleEvent(
                    event = event.uid(),
                    programStage = event.programStage()!!,
                    programStageName =
                        d2
                            .programModule()
                            .programStages()
                            .uid(event.programStage())
                            .blockingGet()!!
                            .name()!!,
                    status =
                        if (event.status() == EventStatus.VISITED) {
                            RuleEventStatus.ACTIVE
                        } else {
                            RuleEventStatus.valueOf(event.status()!!.name)
                        },
                    eventDate = (event.eventDate() ?: Date()).toRuleEngineInstant(),
                    createdDate =
                        event
                            .created()
                            ?.let { Instant.fromEpochMilliseconds(it.time) }
                            ?: Clock.System.now(),
                    dueDate = event.dueDate()?.toRuleEngineLocalDate(),
                    completedDate = event.completedDate()?.toRuleEngineLocalDate(),
                    organisationUnit = event.organisationUnit()!!,
                    organisationUnitCode =
                        d2
                            .organisationUnitModule()
                            .organisationUnits()
                            .uid(event.organisationUnit())
                            .blockingGet()
                            ?.code(),
                    dataValues =
                        event.trackedEntityDataValues()?.toRuleDataValue() ?: emptyList(),
                )
            }.toList()

    private fun programRules(
        programUid: String,
        eventUid: String? = null,
    ) = d2
        .programModule()
        .programRules()
        .byProgramUid()
        .eq(programUid)
        .withProgramRuleActions()
        .get()
        .map { it.toRuleList() }
        .map {
            if (eventUid != null) {
                val programStage =
                    d2
                        .eventModule()
                        .events()
                        .uid(eventUid)
                        .blockingGet()
                        ?.programStage()
                it.filter { rule ->
                    rule.programStage == null || rule.programStage == programStage
                }
            } else {
                it
            }
        }

    private fun entryDataValues(
        qty: String?,
        transaction: Transaction,
        stockUseCase: StockUseCase,
    ): List<RuleDataValue> {
        val values = mutableListOf<RuleDataValue>()

        // Add the quantity if defined, and valid (signs (+/-) could come as streams if incomplete)
        if (qty != null && NumberUtils.isCreatable(qty)) {
            val deUid =
                ConfigUtils.getTransactionDataElement(transaction.transactionType, stockUseCase)
            values.add(
                RuleDataValue(
                    dataElement = deUid,
                    value = qty,
                ),
            )

            // Add the 'deliver to' if it's a distribution event
            if (transaction.transactionType == TransactionType.DISTRIBUTION) {
                transaction.distributedTo?.let { distributedTo ->
                    d2
                        .optionModule()
                        .options()
                        .uid(distributedTo.uid)
                        .blockingGet()
                        ?.code()
                        ?.let { code ->
                            values.add(
                                RuleDataValue(
                                    dataElement = stockUseCase.distributedTo(),
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
