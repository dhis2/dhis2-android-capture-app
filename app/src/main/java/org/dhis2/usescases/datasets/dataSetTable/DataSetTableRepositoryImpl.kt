package org.dhis2.usescases.datasets.dataSetTable

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function4
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import javax.inject.Singleton
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.validation.engine.ValidationResult

@Singleton
class DataSetTableRepositoryImpl(
    private val d2: D2,
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val catOptCombo: String
) {

    private val dataSetInstanceProcessor: FlowableProcessor<Unit> = PublishProcessor.create()
    private val missingMandatoryFieldsProcessor: FlowableProcessor<List<DataElementOperand>> =
        PublishProcessor.create()
    private val missingCompleteDataElementsProcessor: FlowableProcessor<List<String>> =
        PublishProcessor.create()

    fun getDataSet(): Single<DataSet> {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get()
    }

    fun getPeriod(): Single<Period> {
        return d2.periodModule().periodHelper().getPeriodForPeriodId(periodId)
    }

    fun dataSetInstance(): Flowable<DataSetInstance> {
        return dataSetInstanceProcessor.startWith(Unit())
            .switchMap {
                d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catOptCombo)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(periodId).one().exists().toFlowable()
            }
            .flatMap { exist ->
                if (exist) {
                    d2.dataSetModule().dataSetInstances()
                        .byDataSetUid().eq(dataSetUid)
                        .byAttributeOptionComboUid().eq(catOptCombo)
                        .byOrganisationUnitUid().eq(orgUnitUid)
                        .byPeriod().eq(periodId).one().get().toFlowable()
                } else {
                    defaultDataSetInstance()
                }
            }
    }

    private fun defaultDataSetInstance(): Flowable<DataSetInstance> {
        return Single.zip(
            d2.dataSetModule().dataSets().uid(dataSetUid).get(),
            d2.categoryModule().categoryOptionCombos().uid(catOptCombo).get(),
            d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).get(),
            d2.periodModule().periodHelper().getPeriodForPeriodId(periodId),
            Function4 {
                dataSet: DataSet,
                catOptComb: CategoryOptionCombo,
                orgUnit: OrganisationUnit,
                period: Period ->
                DataSetInstance.builder()
                    .dataSetUid(dataSetUid)
                    .dataSetDisplayName(dataSet.displayName())
                    .attributeOptionComboUid(catOptComb.uid())
                    .attributeOptionComboDisplayName(catOptComb.displayName())
                    .organisationUnitUid(orgUnitUid)
                    .organisationUnitDisplayName(orgUnit.displayName())
                    .periodType(period.periodType())
                    .period(period.periodId())
                    .valueCount(0)
                    .completed(false)
                    .state(State.SYNCED)
                    .build()
            }
        ).toFlowable()
    }

    fun getSections(): Flowable<List<String>> {
        return d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).get()
            .map { sections ->
                if (sections.isEmpty()) {
                    arrayListOf("NO_SECTION")
                } else {
                    sections.map { it.displayName() }
                }
            }.toFlowable()
    }

    fun dataSetStatus(): Flowable<Boolean> {
        val dscr = d2.dataSetModule().dataSetCompleteRegistrations()
            .byDataSetUid().eq(dataSetUid)
            .byAttributeOptionComboUid().eq(catOptCombo)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byPeriod().eq(periodId).one().blockingGet()
        return Flowable.just(dscr != null && (dscr.deleted() == null || !dscr.deleted()!!))
    }

    fun dataSetState(): Flowable<State> {
        return Flowable.defer {
            var state: State?
            val dataSetInstance = d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId).one().blockingGet()
            state = dataSetInstance.state()
            val dscr =
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catOptCombo)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(periodId).one().blockingGet()
            if (state == State.SYNCED && dscr != null) {
                state = dscr.state()
            }
            if (state != null) Flowable.just<State>(
                state
            ) else Flowable.empty()
        }
    }

    fun getCatComboName(catcomboUid: String): Flowable<String> {
        return Flowable.fromCallable {
            d2.categoryModule().categoryOptionCombos().uid(catcomboUid).blockingGet()
                .displayName()
        }
    }

    fun getCatOptComboFromOptionList(catOpts: List<String>): String {
        return if (catOpts.isEmpty()) d2.categoryModule().categoryOptionCombos().byDisplayName()
            .like("default").one().blockingGet().uid() else d2.categoryModule()
            .categoryOptionCombos().byCategoryOptions(catOpts).one().blockingGet().uid()
    }

    fun getDataSetCatComboName(): Single<String> {
        return if (catOptCombo != null) {
            d2.categoryModule().categoryOptionCombos().uid(catOptCombo).get()
                .map { categoryOptionCombo: CategoryOptionCombo ->
                    categoryOptionCombo.categoryCombo()!!.uid()
                }
                .flatMap { catComboUid: String? ->
                    d2.categoryModule().categoryCombos().uid(catComboUid).get()
                }
                .map { obj: CategoryCombo -> obj.displayName() }
        } else {
            Single.just("")
        }
    }

    fun completeDataSetInstance(): Completable {
        return Completable.fromSingle(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .value(periodId, orgUnitUid, dataSetUid, catOptCombo).exists()
                .map { alreadyCompleted: Boolean? ->
                    if (!alreadyCompleted!!) {
                        d2.dataSetModule().dataSetCompleteRegistrations()
                            .value(periodId, orgUnitUid, dataSetUid, catOptCombo)
                            .blockingSet()
                    }
                    true
                }
        ).doOnComplete { dataSetInstanceProcessor.onNext(Unit()) }
    }

    fun reopenDataSet(): Flowable<Boolean> {
        d2.dataSetModule().dataSetCompleteRegistrations()
            .value(
                periodId,
                orgUnitUid,
                dataSetUid,
                catOptCombo
            ).blockingDeleteIfExist()
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .value(
                periodId,
                orgUnitUid,
                dataSetUid,
                catOptCombo
            ).exists()
            .map { exist ->
                dataSetInstanceProcessor.onNext(Unit())
                !exist
            }
            .toFlowable()
    }

    fun checkMandatoryFields(): Single<List<DataElementOperand>> {
        return d2.dataSetModule().dataSets().withCompulsoryDataElementOperands().uid(dataSetUid)
            .get()
            .map {
                it.compulsoryDataElementOperands()?.filter { dataElementOperand ->
                    !d2.dataValueModule().dataValues()
                        .value(
                            periodId,
                            orgUnitUid,
                            dataElementOperand.dataElement()?.uid(),
                            dataElementOperand.categoryOptionCombo()?.uid(),
                            catOptCombo
                        ).blockingExists()
                }
            }
            .map { dataElementOperands ->
                if (dataElementOperands.isNotEmpty()) {
                    missingMandatoryFieldsProcessor.onNext(dataElementOperands)
                }
                dataElementOperands
            }
    }

    fun checkFieldCombination(): Single<List<String>> {
        return d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).get()
            .map { dataSet ->
                if (dataSet.fieldCombinationRequired() == true) {
                    dataSet.dataSetElements()
                        ?.filter { dataSetElement ->
                            val catComboUid = dataSetElement.categoryCombo()?.uid()
                                ?: d2.dataElementModule().dataElements()
                                    .uid(
                                        dataSetElement.dataElement().uid()
                                    ).blockingGet().categoryComboUid()
                            val categoryOptionCombos =
                                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                                    .eq(catComboUid).blockingGet()
                            val dataValueRepository = d2.dataValueModule().dataValues()
                                .byPeriod().eq(periodId)
                                .byOrganisationUnitUid().eq(orgUnitUid)
                                .byCategoryOptionComboUid().eq(catOptCombo)
                                .byDataElementUid().eq(dataSetElement.dataElement().uid())
                                .byCategoryOptionComboUid()
                                .`in`(UidsHelper.getUidsList(categoryOptionCombos))
                            dataValueRepository.blockingGet().isNotEmpty() &&
                                dataValueRepository.blockingGet().size != categoryOptionCombos.size
                        }?.map { dataSetElement -> dataSetElement.dataElement().uid() }
                        ?: emptyList()
                } else {
                    emptyList()
                }
            }.map { dataElementsUids ->
                if (dataElementsUids.isNotEmpty()) {
                    missingCompleteDataElementsProcessor.onNext(dataElementsUids)
                }
                dataElementsUids
            }
    }

    fun hasToRunValidationRules(): Boolean {
        return d2.dataSetModule()
            .dataSets().uid(dataSetUid)
            .blockingGet().validCompleteOnly() ?: false
    }

    fun isValidationRuleOptional(): Boolean {
        return d2.validationModule().validationRules().blockingIsEmpty()
    }

    fun executeValidationRules(): Flowable<ValidationResult> {
        return d2.validationModule()
            .validationEngine().validate(dataSetUid, periodId, orgUnitUid, catOptCombo)
            .toFlowable()
    }
}
