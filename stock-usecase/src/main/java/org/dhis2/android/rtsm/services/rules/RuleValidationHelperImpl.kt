package org.dhis2.android.rtsm.services.rules

import io.reactivex.Flowable
import io.reactivex.Single
import java.util.Date
import java.util.Objects
import java.util.UUID
import javax.inject.Inject
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
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import timber.log.Timber

class RuleValidationHelperImpl @Inject constructor(
    private val d2: D2
) : RuleValidationHelper {

    override fun evaluate(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String?,
        appConfig: AppConfig
    ): Flowable<List<RuleEffect>> {
        return ruleEngine(entry.item.id, appConfig.program).flatMap { ruleEngine ->
            val programStage = programStage(program)

            Flowable.fromCallable(
                prepareForDataEntry(ruleEngine, programStage, transaction, entry.date)
            ).flatMap { prelimRuleEffects ->
                val dataValues = mutableListOf<RuleDataValue>().apply {
                    addAll(
                        entryDataValues(
                            entry.qty,
                            programStage.uid(),
                            transaction,
                            entry.date,
                            appConfig
                        )
                    )
                }

                prelimRuleEffects.forEach { ruleEffect ->
                    when (ruleEffect.ruleAction()) {
                        is RuleActionAssign -> {
                            val ruleAction = ruleEffect.ruleAction() as RuleActionAssign
                            ruleEffect.data()?.let { data ->
                                dataValues.add(
                                    RuleDataValue.create(
                                        entry.date,
                                        programStage.uid(),
                                        ruleAction.field(),
                                        data
                                    )
                                )
                            }
                        }
                    }
                }

                printRuleEffects("Preliminary RuleEffects", prelimRuleEffects, dataValues)

                Flowable.fromCallable(
                    ruleEngine.evaluate(
                        createRuleEvent(
                            programStage,
                            transaction.facility.uid,
                            dataValues,
                            entry.date,
                            null
                        )
                    )
                )
            }
        }
    }

    /**
     * Evaluate the program rules on a blank new rule event in preparation for
     * data entry
     */
    private fun prepareForDataEntry(
        ruleEngine: RuleEngine,
        programStage: ProgramStage,
        transaction: Transaction,
        eventDate: Date
    ) = ruleEngine.evaluate(
        createRuleEvent(programStage, transaction.facility.uid, listOf(), eventDate)
    )

    private fun createRuleEvent(
        programStage: ProgramStage,
        organisationUnit: String,
        dataValues: List<RuleDataValue>,
        period: Date,
        eventUid: String? = null
    ) = RuleEvent.create(
        eventUid ?: UUID.randomUUID().toString(), programStage.uid(),
        RuleEvent.Status.ACTIVE, period, period,
        organisationUnit, null, dataValues,
        programStage.name() ?: "", period
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
                    d2.optionModule().options()
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

    private fun ruleEngine(teiUid: String, programUid: String): Flowable<RuleEngine> {
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
            enrollmentEvents
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
                RuleEvent.builder()
                    .event(event.uid())
                    .programStage(event.programStage())
                    .programStageName(
                        d2.programModule().programStages().uid(event.programStage())
                            .blockingGet()!!.name()
                    )
                    .status(
                        if (event.status() == EventStatus.VISITED) {
                            RuleEvent.Status.ACTIVE
                        } else {
                            RuleEvent.Status.valueOf(event.status()!!.name)
                        }
                    )
                    .eventDate(event.eventDate())
                    .dueDate(if (event.dueDate() != null) event.dueDate() else event.eventDate())
                    .organisationUnit(event.organisationUnit())
                    .organisationUnitCode(
                        d2.organisationUnitModule()
                            .organisationUnits().uid(event.organisationUnit())
                            .blockingGet()!!.code()
                    )
                    .dataValues(
                        event.trackedEntityDataValues()?.toRuleDataValue(
                            event,
                            d2.dataElementModule().dataElements(),
                            d2.programModule().programRuleVariables(),
                            d2.optionModule().options()
                        )
                    )
                    .build()
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
                        .uid(eventUid).blockingGet().programStage()
                    it.filter { rule ->
                        rule.programStage() == null || rule.programStage() == programStage
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
        appConfig: AppConfig
    ): List<RuleDataValue> {
        val values = mutableListOf<RuleDataValue>()

        // Add the quantity if defined, and valid (signs (+/-) could come as streams if incomplete)
        if (qty != null && NumberUtils.isCreatable(qty)) {
            val deUid =
                ConfigUtils.getTransactionDataElement(transaction.transactionType, appConfig)
            values.add(RuleDataValue.create(eventDate, programStage, deUid, qty))

            // Add the 'deliver to' if it's a distribution event
            if (transaction.transactionType == TransactionType.DISTRIBUTION) {
                transaction.distributedTo?.let { distributedTo ->
                    d2.optionModule()
                        .options()
                        .uid(distributedTo.uid)
                        .blockingGet()
                        .code()?.let { code ->
                            values.add(
                                RuleDataValue.create(
                                    eventDate,
                                    programStage,
                                    appConfig.distributedTo,
                                    code
                                )
                            )
                        }
                }
            }
        }

        return values.toList()
    }
}
