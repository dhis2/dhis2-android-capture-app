package org.dhis2.android.rtsm.services

import androidx.lifecycle.liveData
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.math.NumberUtils
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.data.AppConfig
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
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject

class StockManagerImpl @Inject constructor(
    val d2: D2,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val ruleValidationHelper: RuleValidationHelper,
    private val dispatcher: StockDispatcherProvider,
) : StockManager {

    override suspend fun search(
        query: SearchParametersModel,
        ou: String?,
        config: AppConfig,
    ): SearchResult {
        val list = withContext(dispatcher.io()) {
            var teiRepository = d2.trackedEntityModule().trackedEntityInstanceQuery()

            if (!ou.isNullOrEmpty()) {
                teiRepository.byOrgUnits()
                    .eq(ou)
                    .byOrgUnitMode()
                    .eq(OrganisationUnitMode.SELECTED)
                    .also { teiRepository = it }
            }

            teiRepository.byProgram()
                .eq(config.program)
                .also { teiRepository = it }

            if (!query.name.isNullOrEmpty()) {
                teiRepository
                    .byQuery()
                    .like(query.name).also { teiRepository = it }
            }

            if (!query.code.isNullOrEmpty()) {
                teiRepository
                    .byQuery()
                    .eq(query.code)
                    .also { teiRepository = it }
            }

            teiRepository.orderByAttribute(config.itemName)
                .eq(RepositoryScope.OrderByDirection.ASC)
                .also { teiRepository = it }

            val teiList = teiRepository.blockingGet()
                .filter { it.deleted() == null || !it.deleted()!! }
                .map { transform(it, config) }

            teiList
        }

        return SearchResult(liveData { emit(list) })
    }

    private fun transform(tei: TrackedEntityInstance, config: AppConfig): StockItem {
        return StockItem(
            tei.uid(),
            AttributeHelper.teiAttributeValueByAttributeUid(tei, config.itemName) ?: "",
            getStockOnHand(tei, config.stockOnHand) ?: "",
        )
    }

    private fun getStockOnHand(tei: TrackedEntityInstance, stockOnHandUid: String): String? {
        val events = d2.eventModule()
            .events()
            .byTrackedEntityInstanceUids(Collections.singletonList(tei.uid()))
            .byDataValue(stockOnHandUid).like("")
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
            .blockingGet()

        events.forEach { event ->
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
            EventCreateProjection.builder()
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
        appConfig: AppConfig,
    ): Single<Unit> {
        Timber.i("SAVING TRANSACTION")

        val programStage = d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(appConfig.program)
            .one()
            .blockingGet() ?: return Single.just(Unit)

        items.forEach { entry ->
            getEnrollment(entry.item.id)?.let { enrollment ->
                createEvent(entry, programStage, enrollment, transaction, appConfig)
            }
        }
        return Single.just(Unit)
    }

    private fun createEvent(
        item: StockEntry,
        programStage: ProgramStage,
        enrollment: Enrollment,
        transaction: Transaction,
        appConfig: AppConfig,
    ) {
        val eventUid = try {
            createEventProjection(
                transaction.facility,
                programStage,
                enrollment,
                appConfig.program,
            )
        } catch (e: Exception) {
            if (e is D2Error) {
                e.originalException()?.printStackTrace()
                Timber.e("Unable to save event: %s", e.errorCode().toString())
            } else {
                e.printStackTrace()
            }
            null
        }
        if (eventUid != null) {
            try {
                // Set the event date
                d2.eventModule().events().uid(eventUid).setEventDate(item.date)
            } catch (e: Exception) {
                if (e is D2Error) {
                    e.originalException()?.printStackTrace()
                    Timber.e("Unable to set event date: %s", e.errorCode().toString())
                } else {
                    e.printStackTrace()
                }
            }

            try {
                Timber.i("event:$eventUid")
                Timber.i("de:${getTransactionDataElement(transaction.transactionType, appConfig)}")
                Timber.i("data to save:${item.qty}")
                d2.trackedEntityModule().trackedEntityDataValues().value(
                    eventUid,
                    getTransactionDataElement(transaction.transactionType, appConfig),
                ).blockingSet(item.qty.toString())
            } catch (e: Exception) {
                if (e is D2Error) {
                    e.originalException()?.printStackTrace()
                    Timber.e("Unable to set value for event: %s\n", e.errorCode().toString())
                } else {
                    e.printStackTrace()
                }
            }

            try {
                transaction.distributedTo?.let {
                    val destination = d2.optionModule()
                        .options()
                        .uid(it.uid)
                        .blockingGet()

                    d2.trackedEntityModule().trackedEntityDataValues().value(
                        eventUid,
                        appConfig.distributedTo,
                    ).blockingSet(destination?.code())
                }
            } catch (e: Exception) {
                if (e is D2Error) {
                    e.originalException()?.printStackTrace()
                    Timber.e("Unable to set destination for event: %s", e.errorCode().toString())
                } else {
                    e.printStackTrace()
                }
            }

            try {
                updateStockOnHand(item, appConfig.program, transaction, eventUid, appConfig)
            } catch (e: Exception) {
                if (e is D2Error) {
                    e.originalException()?.printStackTrace()
                    Timber.e("Unable to update", e.errorCode().toString())
                } else {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateStockOnHand(
        entry: StockEntry,
        program: String,
        transaction: Transaction,
        eventUid: String,
        appConfig: AppConfig,
    ) {
        disposable.add(
            ruleValidationHelper.evaluate(entry, program, transaction, eventUid, appConfig)
                .doOnError { it.printStackTrace() }
                .observeOn(schedulerProvider.io())
                .subscribeOn(schedulerProvider.ui())
                .subscribe { ruleEffects -> performRuleActions(ruleEffects, eventUid) },
        )
    }

    private fun performRuleActions(ruleEffects: List<RuleEffect>?, eventUid: String) {
        Timber.d("Rule Effects: %s", ruleEffects)
        ruleEffects?.forEach { ruleEffect ->
            if (ruleEffect.ruleAction.type == ProgramRuleActionType.ASSIGN.name) {
                val ruleAssign = ruleEffect.ruleAction
                val de = ruleAssign.field()!!
                val value = ruleEffect.data
                if (de.isNotEmpty() && !value.isNullOrEmpty()) {
                    Timber.d("++++      Assigning rule actions:")
                    println("Event uid: $eventUid, dvUid: $de, value: $value")

                    if (NumberUtils.isCreatable(value)) {
                        try {
                            d2.trackedEntityModule()
                                .trackedEntityDataValues()
                                .value(eventUid, de)
                                .blockingSet(value)

                            println("Added data value '$value' to DE $de - Event($eventUid)")
                        } catch (e: Exception) {
                            e.printStackTrace()

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

    private fun getEnrollment(teiUid: String): Enrollment? {
        return d2.enrollmentModule()
            .enrollments()
            .byTrackedEntityInstance()
            .eq(teiUid)
            .one()
            .blockingGet()
    }
}
