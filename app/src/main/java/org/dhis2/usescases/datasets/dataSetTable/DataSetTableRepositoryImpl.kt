package org.dhis2.usescases.datasets.dataSetTable

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function4
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.Date
import javax.inject.Singleton
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection
import org.dhis2.utils.validationrules.DataToReview
import org.dhis2.utils.validationrules.ValidationRuleResult
import org.dhis2.utils.validationrules.Violation
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
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation

@Singleton
class DataSetTableRepositoryImpl(
    private val d2: D2,
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val catOptCombo: String,
    private val resourceManager: ResourceManager
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

    fun getOrgUnit(): Single<OrganisationUnit> {
        return d2.organisationUnitModule().organisationUnits()
            .uid(orgUnitUid)
            .get()
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
            Function4 { dataSet: DataSet,
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
                    .lastUpdated(Date())
                    .state(State.SYNCED)
                    .build()
            }
        ).toFlowable()
    }

    fun getSections(): Flowable<List<DataSetSection>> {
        return d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).get()
            .map { sections ->
                if (sections.isEmpty()) {
                    arrayListOf(
                        DataSetSection(
                            "NO_SECTION",
                            resourceManager.defaultEmptyDataSetSectionLabel()
                        )
                    )
                } else {
                    sections.map { DataSetSection(it.uid(), it.displayName()) }
                }
            }.toFlowable()
    }

    fun getSectionIndexWithErrors(defaultSectionIndex: Int = 0): Int {
        val sections = d2.dataSetModule().sections()
            .byDataSetUid().eq(dataSetUid)
            .withDataElements()
            .blockingGet().associate {
                it.uid() to it.dataElements()?.map { dataElement -> dataElement.uid() }
            }

        val sectionWithError = d2.dataValueModule().dataValueConflicts()
            .byDataSet(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionCombo().eq(catOptCombo)
            .blockingGet()?.mapNotNull { dataValueConflict ->
                dataValueConflict.dataElement()?.let { dataElementUid ->
                    sections.filter { it.value?.contains(dataElementUid) == true }.keys
                }
            }?.flatten()

        return sectionWithError?.firstOrNull()?.let {
            sections.keys.indexOf(it)
        } ?: defaultSectionIndex
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
            if (state != null) {
                Flowable.just<State>(
                    state
                )
            } else {
                Flowable.empty()
            }
        }
    }

    fun getCatComboName(): Flowable<String> {
        return Flowable.fromCallable {
            d2.categoryModule().categoryOptionCombos().uid(catOptCombo).blockingGet()
                .displayName()
        }
    }

    fun getCatOptComboFromOptionList(catOpts: List<String>): String {
        return if (catOpts.isEmpty()) {
            d2.categoryModule().categoryOptionCombos().byDisplayName()
                .like("default").one().blockingGet().uid()
        } else {
            d2.categoryModule()
                .categoryOptionCombos().byCategoryOptions(catOpts).one().blockingGet().uid()
        }
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

    fun completeDataSetInstance(): Single<Boolean> {
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .value(periodId, orgUnitUid, dataSetUid, catOptCombo).exists()
            .map { hasCompleteRegistration: Boolean? ->
                val hasValidCompleteRegistration = if (hasCompleteRegistration == true) {
                    d2.dataSetModule().dataSetCompleteRegistrations()
                        .value(periodId, orgUnitUid, dataSetUid, catOptCombo).blockingGet()
                        .deleted() != true
                } else {
                    false
                }
                if (!hasValidCompleteRegistration) {
                    d2.dataSetModule().dataSetCompleteRegistrations()
                        .value(periodId, orgUnitUid, dataSetUid, catOptCombo)
                        .blockingSet()
                    return@map false
                }
                return@map true
            }.doOnSuccess { dataSetInstanceProcessor.onNext(Unit()) }
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
                val hasBeenReopened = if (exist) {
                    d2.dataSetModule().dataSetCompleteRegistrations()
                        .value(
                            periodId,
                            orgUnitUid,
                            dataSetUid,
                            catOptCombo
                        ).blockingGet().deleted() == true
                } else {
                    true
                }
                dataSetInstanceProcessor.onNext(Unit())
                hasBeenReopened
            }
            .toFlowable()
    }

    fun checkMandatoryFields(): Single<List<DataElementOperand>> {
        return d2.dataSetModule().dataSets().withCompulsoryDataElementOperands().uid(dataSetUid)
            .get()
            .map {
                it.compulsoryDataElementOperands()?.filter { dataElementOperand ->
                    dataElementOperand.dataElement()?.let { dataElement ->
                        dataElementOperand.categoryOptionCombo()?.let { categoryOptionCombo ->
                            !d2.dataValueModule().dataValues()
                                .value(
                                    periodId,
                                    orgUnitUid,
                                    dataElement.uid(),
                                    categoryOptionCombo.uid(),
                                    catOptCombo
                                ).blockingExists()
                        }
                    } ?: false
                }
            }
            .map { dataElementOperands ->
                if (dataElementOperands.isNotEmpty()) {
                    missingMandatoryFieldsProcessor.onNext(dataElementOperands)
                }
                dataElementOperands
            }
    }

    fun checkFieldCombination(): Single<Pair<Boolean, List<String>>> {
        return d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).get()
            .map { dataSet ->
                if (dataSet.fieldCombinationRequired() == true) {
                    dataSet.dataSetElements()
                        ?.filter { dataSetElement ->
                            val catComboUid = dataSetElement.categoryCombo()?.uid()
                                ?: d2.dataElementModule().dataElements()
                                    .uid(dataSetElement.dataElement().uid())
                                    .blockingGet().categoryComboUid()
                            val categoryOptionCombos =
                                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                                    .eq(catComboUid).blockingGet()
                            val dataValueRepository = d2.dataValueModule().dataValues()
                                .byPeriod().eq(periodId)
                                .byOrganisationUnitUid().eq(orgUnitUid)
                                .byAttributeOptionComboUid().eq(catOptCombo)
                                .byDeleted().isFalse
                                .byDataElementUid().eq(dataSetElement.dataElement().uid())
                                .byCategoryOptionComboUid()
                                .`in`(UidsHelper.getUidsList(categoryOptionCombos))
                            dataValueRepository.blockingGet().isNotEmpty() &&
                                dataValueRepository
                                .blockingGet().size != categoryOptionCombos.size
                        }?.map { dataSetElement -> dataSetElement.dataElement().uid() }
                        ?: emptyList()
                } else {
                    emptyList()
                }
            }.map { dataElementsUids ->
                if (dataElementsUids.isNotEmpty()) {
                    missingCompleteDataElementsProcessor.onNext(dataElementsUids)
                }
                Pair.create(dataElementsUids.isEmpty(), dataElementsUids)
            }
    }

    fun areValidationRulesMandatory(): Boolean {
        return d2.dataSetModule()
            .dataSets().uid(dataSetUid)
            .blockingGet().validCompleteOnly() ?: false
    }

    fun hasValidationRules(): Boolean {
        return !d2.validationModule().validationRules()
            .byDataSetUids(listOf(dataSetUid))
            .bySkipFormValidation().isFalse
            .blockingIsEmpty()
    }

    fun executeValidationRules(): Flowable<ValidationRuleResult> {
        return d2.validationModule()
            .validationEngine().validate(dataSetUid, periodId, orgUnitUid, catOptCombo)
            .map {
                ValidationRuleResult(
                    it.status(),
                    mapViolations(it.violations())
                )
            }
            .toFlowable()
    }

    private fun mapViolations(violations: List<ValidationResultViolation>): List<Violation> {
        return violations.map {
            Violation(
                it.validationRule().description(),
                it.validationRule().instruction(),
                mapDataElements(it.dataElementUids())
            )
        }
    }

    private fun mapDataElements(
        dataElementUids: MutableSet<DataElementOperand>
    ): List<DataToReview> {
        val dataToReview = arrayListOf<DataToReview>()
        for (deOperand in dataElementUids) {
            val de =
                d2.dataElementModule().dataElements()
                    .uid(deOperand.dataElement()?.uid())
                    .blockingGet()
            val catOptCombos =
                if (deOperand.categoryOptionCombo() != null) {
                    d2.categoryModule().categoryOptionCombos()
                        .byUid().like(deOperand.categoryOptionCombo()?.uid())
                        .blockingGet()
                } else {
                    d2.categoryModule().categoryOptionCombos()
                        .byCategoryComboUid().like(de.categoryComboUid())
                        .blockingGet()
                }
            catOptCombos.forEach { catOptCombo ->
                val value = if (d2.dataValueModule().dataValues()
                    .value(periodId, orgUnitUid, de.uid(), catOptCombo.uid(), this.catOptCombo)
                    .blockingExists() &&
                    d2.dataValueModule().dataValues()
                        .value(periodId, orgUnitUid, de.uid(), catOptCombo.uid(), this.catOptCombo)
                        .blockingGet().deleted() != true
                ) {
                    d2.dataValueModule().dataValues()
                        .value(periodId, orgUnitUid, de.uid(), catOptCombo.uid(), this.catOptCombo)
                        .blockingGet().value() ?: "-"
                } else {
                    "-"
                }
                val isFromDefaultCatCombo = d2.categoryModule().categoryCombos()
                    .uid(catOptCombo.categoryCombo()?.uid()).blockingGet().isDefault == true
                dataToReview.add(
                    DataToReview(
                        de.uid(),
                        de.displayName(),
                        catOptCombo.uid(),
                        catOptCombo.displayName(),
                        value,
                        isFromDefaultCatCombo
                    )
                )
            }
        }
        return dataToReview
    }

    fun isComplete(): Single<Boolean> {
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .byDataSetUid().eq(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionComboUid().eq(catOptCombo)
            .byDeleted().isFalse
            .isEmpty
            .map { isEmpty -> !isEmpty }
    }

    fun hasDataElementDecoration(): Boolean {
        return d2.dataSetModule().dataSets().uid(dataSetUid)
            .blockingGet()
            .dataElementDecoration() == true
    }
}
