package org.dhis2.android.rtsm.services

import androidx.lifecycle.liveData
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.math.NumberUtils
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.data.models.IdentifiableModel
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.SearchResult
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.utils.AttributeHelper
import org.dhis2.android.rtsm.utils.ConfigUtils.getTransactionDataElement
import org.dhis2.commons.bindings.distributedTo
import org.dhis2.commons.bindings.stockUseCase
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.Collections

class StockManagerImpl(
    val d2: D2,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val ruleValidationHelper: RuleValidationHelper,
    private val dispatcher: StockDispatcherProvider,
) : StockManager {
    override suspend fun search(
        query: SearchParametersModel,
        ou: String?,
        config: StockUseCase,
    ): SearchResult {
        val list =
            withContext(dispatcher.io()) {
                var teiRepository = d2.trackedEntityModule().trackedEntityInstanceQuery()

                if (!ou.isNullOrEmpty()) {
                    teiRepository
                        .byOrgUnits()
                        .eq(ou)
                        .byOrgUnitMode()
                        .eq(OrganisationUnitMode.SELECTED)
                        .also { teiRepository = it }
                }

                teiRepository
                    .byProgram()
                    .eq(config.programUid)
                    .also { teiRepository = it }

                if (!query.name.isNullOrEmpty()) {
                    teiRepository
                        .byQuery()
                        .like(query.name)
                        .also { teiRepository = it }
                }

                if (!query.code.isNullOrEmpty()) {
                    teiRepository
                        .byQuery()
                        .eq(query.code)
                        .also { teiRepository = it }
                }

                teiRepository
                    .orderByAttribute(config.itemDescription)
                    .eq(RepositoryScope.OrderByDirection.ASC)
                    .also { teiRepository = it }

                val teiList =
                    teiRepository
                        .blockingGet()
                        .filter { it.deleted() == null || !it.deleted()!! }
                        .map { transform(it, config) }

                teiList
            }

        return SearchResult(liveData { emit(list) })
    }

    private fun transform(
        tei: TrackedEntityInstance,
        config: StockUseCase,
    ): StockItem {
        val optionSet =
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(config.itemDescription)
                .blockingGet()
                ?.optionSet()

        return StockItem(
            tei.uid(),
            AttributeHelper.teiAttributeValueByAttributeUid(
                tei,
                config.itemDescription,
                optionSet != null,
            ) { code ->
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSet?.uid())
                    .byCode()
                    .eq(code)
                    .one()
                    .blockingGet()
                    ?.displayName() ?: ""
            } ?: "",
            getStockOnHand(tei, config.stockOnHand) ?: "",
        )
    }

    private fun getStockOnHand(
        tei: TrackedEntityInstance,
        stockOnHandUid: String,
    ): String? {
        val events =
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(tei.uid()))
                .byDataValue(stockOnHandUid)
                .like("")
                .byDeleted()
                .isFalse
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet()

        events
            .filter {
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(it.enrollment())
                    .blockingGet()
                    ?.status() == EnrollmentStatus.ACTIVE
            }.forEach { event ->
                event.trackedEntityDataValues()?.forEach { dataValue ->
                    dataValue.dataElement().let { dv ->
                        if (dv.equals(stockOnHandUid)) {
                            return dataValue.value()
                        }
                    }
                }
            }

        return null
    }

    private fun createEventProjection(
        facility: IdentifiableModel,
        programStage: ProgramStage,
        enrollment: Enrollment,
        programUid: String,
    ): String {
        Timber.tag("EVENT_CREATION").i(
            "Enrollment: ${enrollment.uid()}\n" +
                "Program: ${programUid}\n" +
                "Stage: ${programStage.uid()}\n" +
                "OU: ${facility.uid}\n",
        )
        return d2.eventModule().events().blockingAdd(
            EventCreateProjection
                .builder()
                .enrollment(enrollment.uid())
                .program(programUid)
                .programStage(programStage.uid())
                .organisationUnit(facility.uid)
                .build(),
        )
    }

    override fun saveTransaction(
        items: List<StockEntry>,
        transaction: Transaction,
        stockUseCase: StockUseCase,
        hasErrorOnComplete: Boolean,
    ): Single<Unit> {
        Timber.i("SAVING TRANSACTION")

        val programStage =
            d2
                .programModule()
                .programStages()
                .byProgramUid()
                .eq(stockUseCase.programUid)
                .one()
                .blockingGet() ?: return Single.just(Unit)

        items.forEach { entry ->
            getEnrollment(entry.item.id)?.let { enrollment ->
                createEvent(entry, programStage, enrollment, transaction, stockUseCase, hasErrorOnComplete)
            }
        }
        return Single.just(Unit)
    }

    override suspend fun stockUseCase(program: String) =
        withContext(dispatcher.io()) {
            return@withContext d2.stockUseCase(program)
        }

    private fun createEvent(
        item: StockEntry,
        programStage: ProgramStage,
        enrollment: Enrollment,
        transaction: Transaction,
        stockUseCase: StockUseCase,
        hasErrorOnComplete: Boolean,
    ) {
        val eventUid =
            try {
                createEventProjection(
                    transaction.facility,
                    programStage,
                    enrollment,
                    stockUseCase.programUid,
                )
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to save event: %s", e.errorCode().toString())
                } else {
                    Timber.e(e)
                }
                null
            }
        if (eventUid != null) {
            try {
                // Set the event date
                d2
                    .eventModule()
                    .events()
                    .uid(eventUid)
                    .setEventDate(item.date)
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to set event date: %s", e.errorCode().toString())
                } else {
                    Timber.e(e)
                }
            }

            try {
                // set event status as complete
                val hasEmptyMandatoryFields =
                    d2
                        .programModule()
                        .programStageDataElements()
                        .byProgramStage()
                        .eq(programStage.uid())
                        .byCompulsory()
                        .isTrue
                        .blockingGet()
                        .any { programStageDataElement ->
                            val mandatoryDataElement = programStageDataElement.dataElement()?.uid()
                            val dataElementValue =
                                mandatoryDataElement?.let {
                                    d2
                                        .trackedEntityModule()
                                        .trackedEntityDataValues()
                                        .value(eventUid, it)
                                        .blockingGet()
                                        ?.value()
                                }
                            dataElementValue.isNullOrEmpty()
                        }
                val canComplete = hasErrorOnComplete.not() and hasEmptyMandatoryFields.not()
                d2
                    .eventModule()
                    .events()
                    .uid(eventUid)
                    .setStatus(
                        if (canComplete) {
                            EventStatus.COMPLETED
                        } else {
                            EventStatus.ACTIVE
                        },
                    )
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to set event status: %s", e.errorCode().toString())
                } else {
                    Timber.e(e)
                }
            }

            try {
                Timber.i("event:$eventUid")
                Timber.i("de:${getTransactionDataElement(transaction.transactionType, stockUseCase)}")
                Timber.i("data to save:${item.qty}")
                d2
                    .trackedEntityModule()
                    .trackedEntityDataValues()
                    .value(
                        eventUid,
                        getTransactionDataElement(transaction.transactionType, stockUseCase),
                    ).blockingSet(item.qty.toString())
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to set value for event: %s\n", e.errorCode().toString())
                } else {
                    Timber.e(e)
                }
            }

            try {
                transaction.distributedTo?.let {
                    val destination =
                        d2
                            .optionModule()
                            .options()
                            .uid(it.uid)
                            .blockingGet()

                    d2
                        .trackedEntityModule()
                        .trackedEntityDataValues()
                        .value(
                            eventUid,
                            stockUseCase.distributedTo(),
                        ).blockingSet(destination?.code())
                }
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to set destination for event: %s", e.errorCode().toString())
                } else {
                    Timber.e(e)
                }
            }

            try {
                updateStockOnHand(item, stockUseCase.programUid, transaction, eventUid, stockUseCase)
            } catch (e: Exception) {
                if (e is D2Error) {
                    Timber.e(e.originalException())
                    Timber.e("Unable to update")
                } else {
                    Timber.e(e)
                }
            }
        }
    }

    private fun updateStockOnHand(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String,
        stockUseCase: StockUseCase,
    ) {
        disposable.add(
            ruleValidationHelper
                .evaluate(entry, program, transaction, eventUid, stockUseCase)
                .doOnError { e -> Timber.e(e) }
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .subscribe { ruleEffects -> performRuleActions(ruleEffects, eventUid) },
        )
    }

    private fun performRuleActions(
        ruleEffects: List<RuleEffect>?,
        eventUid: String,
    ) {
        Timber.d("Rule Effects: %s", ruleEffects)
        ruleEffects?.forEach { ruleEffect ->
            if (ruleEffect.ruleAction.type == ProgramRuleActionType.ASSIGN.name) {
                val ruleAssign = ruleEffect.ruleAction
                val de = ruleAssign.field()!!
                val value = ruleEffect.data
                if (de.isNotEmpty() && !value.isNullOrEmpty()) {
                    Timber.d("++++      Assigning rule actions:")
                    Timber.d("Event uid: $eventUid, dvUid: $de, value: $value")

                    if (NumberUtils.isCreatable(value)) {
                        try {
                            d2
                                .trackedEntityModule()
                                .trackedEntityDataValues()
                                .value(eventUid, de)
                                .blockingSet(value)

                            Timber.d("Added data value '$value' to DE $de - Event($eventUid)")
                        } catch (e: Exception) {
                            Timber.e(e)

                            if (e is D2Error) {
                                Timber.e(
                                    "Unable to save rule effect data: %s",
                                    e.errorCode().toString(),
                                )
                            }
                        }
                    } else {
                        Timber.w("Unable to assign program action using invalid data: %s", value)
                    }
                }
            }
        }
    }

    private fun getEnrollment(teiUid: String): Enrollment? =
        d2
            .enrollmentModule()
            .enrollments()
            .byStatus()
            .eq(EnrollmentStatus.ACTIVE)
            .byTrackedEntityInstance()
            .eq(teiUid)
            .one()
            .blockingGet()
}
