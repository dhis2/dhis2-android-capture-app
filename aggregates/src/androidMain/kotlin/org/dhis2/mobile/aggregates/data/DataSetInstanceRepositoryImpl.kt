package org.dhis2.mobile.aggregates.data

import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.mobile.aggregates.data.mappers.toCustomTitle
import org.dhis2.mobile.aggregates.data.mappers.toDataSetDetails
import org.dhis2.mobile.aggregates.data.mappers.toDataSetSection
import org.dhis2.mobile.aggregates.data.mappers.toInputType
import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataElementInfo
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataToReview
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.model.MandatoryCellElements
import org.dhis2.mobile.aggregates.model.PivoteMode
import org.dhis2.mobile.aggregates.model.TableGroup
import org.dhis2.mobile.aggregates.model.ValidationResultStatus
import org.dhis2.mobile.aggregates.model.ValidationRulesResult
import org.dhis2.mobile.aggregates.model.Violation
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.commons.validation.validators.FieldMaskValidator
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataSetEditableStatus
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.dataset.SectionPivotMode
import org.hisp.dhis.android.core.dataset.TabsDirection
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation
import java.util.Locale

internal class DataSetInstanceRepositoryImpl(
    private val d2: D2,
    private val periodLabelProvider: PeriodLabelProvider,
) : DataSetInstanceRepository {

    override suspend fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails {
        val dataSet = d2.dataSetModule().dataSets()
            .uid(dataSetUid)
            .blockingGet()

        val catComboUid = dataSet?.categoryCombo()?.uid()
        val isDefaultCatCombo = d2.categoryModule().categoryCombos()
            .uid(catComboUid)
            .blockingGet()
            ?.isDefault

        val dataSetDTOCustomTitle = dataSet?.displayOptions()?.customText()

        return d2.dataSetModule().dataSetInstances()
            .byDataSetUid().eq(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionComboUid().eq(attrOptionComboUid)
            .blockingGet()
            .map { dataSetInstance ->
                val period = d2.periodModule().periods().byPeriodId().eq(dataSetInstance.period())
                    .one().blockingGet()

                dataSetInstance.toDataSetDetails(
                    periodLabel = period?.let {
                        periodLabelProvider(
                            periodType = period.periodType(),
                            periodId = period.periodId()!!,
                            periodStartDate = period.startDate()!!,
                            periodEndDate = period.endDate()!!,
                            locale = Locale.getDefault(),
                        )
                    } ?: dataSetInstance.period(),
                    isDefaultCatCombo = isDefaultCatCombo == true,
                    customText = dataSetDTOCustomTitle,
                )
            }
            .firstOrNull() ?: DataSetDetails(
            customTitle = dataSetDTOCustomTitle.toCustomTitle(),
            dataSetTitle = dataSet?.displayName()!!,
            dateLabel = periodId,
            orgUnitLabel = d2.organisationUnitModule().organisationUnits()
                .uid(orgUnitUid)
                .blockingGet()
                ?.displayName() ?: orgUnitUid,
            catOptionComboLabel = d2.categoryModule().categoryOptionCombos()
                .uid(attrOptionComboUid)
                .blockingGet()
                ?.displayName(),
        )
    }

    override suspend fun getDataSetInstanceSections(
        dataSetUid: String,
    ) = d2.dataSetModule().sections()
        .byDataSetUid().eq(dataSetUid)
        .blockingGet().map(Section::toDataSetSection)

    override suspend fun isComplete(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): Boolean {
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .byDataSetUid().eq(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionComboUid().eq(attrOptionComboUid)
            .byDeleted().isFalse
            .isEmpty()
            .map { isEmpty -> !isEmpty }.blockingGet()
    }

    override suspend fun areValidationRulesMandatory(dataSetUid: String): Boolean {
        return d2.dataSetModule()
            .dataSets().uid(dataSetUid)
            .blockingGet()?.validCompleteOnly() ?: false
    }

    override suspend fun checkIfHasValidationRules(dataSetUid: String): Boolean {
        return !d2.validationModule().validationRules()
            .byDataSetUids(listOf(dataSetUid))
            .bySkipFormValidation().isFalse
            .blockingIsEmpty()
    }

    override suspend fun getRenderingConfig(
        dataSetUid: String,
    ) = d2.dataSetModule().dataSets()
        .uid(dataSetUid)
        .blockingGet()?.let {
            DataSetRenderingConfig(
                useVerticalTabs = it.displayOptions()?.tabsDirection() == TabsDirection.VERTICAL,
            )
        } ?: DataSetRenderingConfig(
        useVerticalTabs = true,
    )

    private suspend fun categoryOptionCombinations(
        categoryUids: List<String>,
        pivotedCategoryUid: String?,
    ): List<String> {
        return categoryUids.mapNotNull { categoryUid ->
            if (categoryUid == pivotedCategoryUid) return@mapNotNull null
            val catOptions = d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(categoryUid)
                .blockingGet()
                ?.categoryOptions()
            catOptions?.mapNotNull { it.uid() }
        }.fold(listOf(listOf<String>())) { acc, list ->
            acc.flatMap { existing ->
                list.map { element ->
                    existing + element
                }
            }
        }.mapNotNull { categoryOptions ->
            if (pivotedCategoryUid == null) {
                d2.categoryModule().categoryOptionCombos()
                    .byCategoryOptions(categoryOptions)
                    .one()
                    .blockingGet()?.uid()
            } else {
                categoryOptions.joinToString("_")
            }
        }
    }

    override suspend fun conflicts(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): Pair<List<String>, List<String>> {
        val conflicts = d2.dataValueModule().dataValueConflicts()
            .byDataSet(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionCombo().eq(attrOptionComboUid)
            .byDataElement().eq(dataElementUid)
            .byCategoryOptionCombo().eq(categoryOptionComboUid)
            .blockingGet()
            .mapNotNull {
                it.conflict()
            }

        val syncState = d2.dataValueModule().dataValues()
            .value(
                periodId,
                orgUnitUid,
                dataElementUid,
                categoryOptionComboUid,
                attrOptionComboUid,
            )
            .blockingGet()?.syncState()

        return when (syncState) {
            State.ERROR -> Pair(conflicts, emptyList())
            State.WARNING -> Pair(emptyList(), conflicts)
            else -> Pair(emptyList(), emptyList())
        }
    }

    override suspend fun dataSetInstanceConfiguration(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        sectionUid: String,
    ): DataSetInstanceConfiguration {
        val dataSet = d2.dataSetModule().dataSets()
            .withDataSetElements()
            .withCompulsoryDataElementOperands()
            .uid(dataSetUid)
            .blockingGet()

        val compulsoryDataElements = dataSet?.compulsoryDataElementOperands()
            ?.mapNotNull { dataElementOperand ->
                MandatoryCellElements(
                    uid = dataElementOperand.uid(),
                    categoryOptionComboUid = dataElementOperand.categoryOptionCombo()?.uid(),
                )
            }

        val allDataSetElements = dataSet?.dataSetElements()
            ?.mapNotNull { dataSetElement ->
                d2.dataElementModule().dataElements()
                    .uid(dataSetElement.dataElement().uid())
                    .blockingGet()?.let { dataElement ->
                        CellElement(
                            uid = dataElement.uid(),
                            categoryComboUid = dataSetElement.categoryCombo()?.uid(),
                            label = dataElement.displayFormName() ?: dataElement.uid(),
                            description = dataElement.displayDescription(),
                            isMultiText = dataElement.valueType() == ValueType.MULTI_TEXT,
                        )
                    }
            }

        val greyedOutFields = d2.dataSetModule().sections()
            .withGreyedFields()
            .withIndicators()
            .uid(sectionUid)
            .blockingGet()
            ?.greyedFields()
            ?.mapNotNull { it.dataElement()?.uid() }

        val isEditable = d2.dataSetModule().dataSetInstanceService()
            .getEditableStatus(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
            .blockingGet() == DataSetEditableStatus.Editable

        return DataSetInstanceConfiguration(
            hasDataElementDecoration = dataSet?.dataElementDecoration() == true,
            compulsoryDataElements = compulsoryDataElements ?: emptyList(),
            allDataSetElements = allDataSetElements ?: emptyList(),
            greyedOutFields = greyedOutFields ?: emptyList(),
            editable = isEditable,
        )
    }

    override suspend fun dataSetInstanceSectionConfiguration(sectionUid: String) =
        d2.dataSetModule().sections()
            .uid(sectionUid)
            .blockingGet()?.let { section ->
                DataSetInstanceSectionConfiguration(
                    showRowTotals = section.showRowTotals() == true,
                    showColumnTotals = section.showColumnTotals() == true,
                    pivotedHeaderId = section.displayOptions()?.pivotedCategory(),
                )
            }

    override suspend fun getDataSetInstanceSectionCells(
        dataSetElements: List<CellElement>,
        dataSetUid: String,
        sectionUid: String,
    ): List<TableGroup> {
        var dataSetElementsInSection = dataSetElements

        val catComboUids = when (sectionUid) {
            NO_SECTION_UID -> {
                dataSetElements.mapNotNull { dataSetElement ->
                    dataSetElement.categoryComboUid ?: dataElementCategoryComboUid(
                        dataSetElement.uid,
                    )
                }.distinct()
            }

            else -> {
                val dataElementUidsInSection = d2.dataSetModule().sections()
                    .withDataElements()
                    .byDataSetUid().eq(dataSetUid)
                    .uid(sectionUid)
                    .blockingGet()
                    ?.dataElements()
                    ?.mapNotNull { it.uid() }

                dataSetElementsInSection = dataSetElements.filter {
                    it.uid in dataElementUidsInSection.orEmpty()
                }.sortedBy { dataElementUidsInSection?.indexOf(it.uid) }

                dataSetElementsInSection
                    .mapNotNull { dataSetElement ->
                        dataSetElement.categoryComboUid ?: dataElementCategoryComboUid(
                            dataSetElement.uid,
                        )
                    }.distinct()
            }
        }

        val sectionData = d2.dataSetModule().sections()
            .uid(sectionUid)
            .blockingGet()

        val pivotedCategoryUid = sectionData?.displayOptions()?.pivotedCategory()
        val disableGrouping = sectionData?.disableDataElementAutoGroup() == true
        val pivoted = sectionData?.displayOptions()?.pivotMode() == SectionPivotMode.PIVOT

        return if (disableGrouping) {
            DisableDataElementGrouping(
                dataSetElementsInSection.map {
                    it.copy(
                        categoryComboUid = it.categoryComboUid
                            ?: dataElementCategoryComboUid(it.uid),
                    )
                },
            ).mapIndexed { index, noGroupingDataSetElements ->
                val mainCellElement = noGroupingDataSetElements.first()
                val catComboUid = mainCellElement.categoryComboUid ?: dataElementCategoryComboUid(
                    mainCellElement.uid,
                )
                val catCombo = d2.categoryModule().categoryCombos()
                    .withCategories()
                    .uid(catComboUid)
                    .blockingGet()!!

                val subGroups = catCombo.categories()?.mapNotNull { it.uid() } ?: emptyList()

                TableGroup(
                    uid = "${catCombo.uid()}_$index",
                    label = catCombo.displayName() ?: "",
                    subgroups = subGroups,
                    cellElements = noGroupingDataSetElements,
                    headerRows = getTableGroupHeaders(catComboUid!!, subGroups, pivotedCategoryUid),
                    headerCombinations = categoryOptionCombinations(subGroups, pivotedCategoryUid),
                    pivotMode = when {
                        pivoted ->
                            PivoteMode.Transpose

                        pivotedCategoryUid != null ->
                            PivoteMode.CategoryToColumn(
                                pivotedHeaders(
                                    pivotedCategoryUid,
                                ),
                            )

                        else -> PivoteMode.None
                    },
                )
            }
        } else {
            d2.categoryModule().categoryCombos()
                .byUid().`in`(catComboUids)
                .withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet().map { catCombo ->

                    val subGroups = catCombo.categories()?.mapNotNull { it.uid() } ?: emptyList()
                    val cellElements = dataSetElementsInSection.filter { dataSetElement ->
                        val catComboUid =
                            dataSetElement.categoryComboUid ?: dataElementCategoryComboUid(
                                dataSetElement.uid,
                            )
                        catComboUid == catCombo.uid()
                    }

                    TableGroup(
                        uid = catCombo.uid(),
                        label = catCombo.displayName() ?: "",
                        subgroups = subGroups,
                        cellElements = cellElements,
                        headerRows = getTableGroupHeaders(
                            catCombo.uid(),
                            subGroups,
                            pivotedCategoryUid,
                        ),
                        headerCombinations = categoryOptionCombinations(
                            subGroups,
                            pivotedCategoryUid,
                        ),
                        pivotMode = when {
                            pivoted ->
                                PivoteMode.Transpose

                            pivotedCategoryUid != null ->
                                PivoteMode.CategoryToColumn(
                                    pivotedHeaders(
                                        pivotedCategoryUid,
                                    ),
                                )

                            else -> PivoteMode.None
                        },
                    )
                }
        }
    }

    override suspend fun getInitialSectionToLoad(
        openErrorLocation: Boolean,
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        catOptCombo: String,
    ): Int {
        return if (openErrorLocation) {
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
            } ?: 0
        } else {
            0
        }
    }

    private fun pivotedHeaders(pivotedCategoryUid: String?) = pivotedCategoryUid?.let {
        d2.categoryModule().categories()
            .withCategoryOptions()
            .uid(it)
            .blockingGet()
            ?.categoryOptions()
            ?.map { catOption ->
                CellElement(
                    uid = catOption.uid(),
                    label = catOption.displayName() ?: catOption.uid(),
                    description = catOption.displayName(),
                    isMultiText = false,
                    categoryComboUid = null,
                )
            }
    } ?: emptyList()

    override suspend fun getLegend(
        dataElementUid: String,
        periodId: String,
        orgUnitUid: String,
        categoryOptionComboUid: String,
        attrOptionComboUid: String,
    ): Pair<ColorString?, LegendLabel?>? {
        val dataElement = d2.dataElementModule().dataElements()
            .uid(dataElementUid)
            .blockingGet()
        if (dataElement?.valueType()?.isNumeric != true) return null

        val legendsSet = d2.dataElementModule().dataElements()
            .withLegendSets()
            .uid(dataElementUid)
            .blockingGet()
            ?.legendSets()

        if (legendsSet.isNullOrEmpty()) return null

        val value = d2.dataValueModule().dataValues()
            .value(
                period = periodId,
                organisationUnit = orgUnitUid,
                dataElement = dataElementUid,
                categoryOptionCombo = categoryOptionComboUid,
                attributeOptionCombo = attrOptionComboUid,
            )
            .blockingGet()
            ?.value()?.toDoubleOrNull()

        if (value == null) return null

        val valueLegend = d2.legendSetModule().legends()
            .byLegendSet().`in`(legendsSet.map { it.uid() })
            .byStartValue().smallerThan(value)
            .byEndValue().biggerOrEqualTo(value)
            .one()
            .blockingGet()

        return Pair(valueLegend?.color(), valueLegend?.displayName())
    }

    private fun getTableGroupHeaders(
        catComboUid: String,
        categoryUids: List<String>,
        pivotedCategoryUid: String?,
    ): List<List<CellElement>> {
        return categoryUids.mapNotNull { categoryUid ->
            if (categoryUid == pivotedCategoryUid) return@mapNotNull null
            val categoryOptions = d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(categoryUid)
                .blockingGet()
                ?.categoryOptions() ?: emptyList()

            if (categoryOptions.isNotEmpty()) {
                categoryOptions.map { categoryOption ->
                    CellElement(
                        uid = categoryOption.uid(),
                        label = categoryOption.displayName() ?: categoryOption.uid(),
                        description = categoryOption.displayName(),
                        isMultiText = false,
                        categoryComboUid = catComboUid,
                    )
                }
            } else {
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid().eq(categoryUid)
                    .blockingGet().map {
                        CellElement(
                            uid = it.uid(),
                            label = it.displayName() ?: it.uid(),
                            description = it.displayName(),
                            isMultiText = false,
                            categoryComboUid = catComboUid,
                        )
                    }
            }
        }
    }

    override suspend fun values(
        periodId: String,
        orgUnitUid: String,
        dataElementUids: List<String>,
        attrOptionComboUid: String,
        pivotedCategoryUid: String?,
    ): List<Pair<Pair<String, String>, String?>> {
        val pivotedCategoryOptionUids = pivotedCategoryUid?.let {
            d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(pivotedCategoryUid)
                .blockingGet()
                ?.categoryOptions()?.map { it.uid() }
        } ?: emptyList()

        return d2.dataValueModule().dataValues()
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionComboUid().eq(attrOptionComboUid)
            .byDataElementUid().`in`(dataElementUids)
            .blockingGet().map {
                val key = if (pivotedCategoryUid.isNullOrEmpty()) {
                    Pair(it.dataElement()!!, it.categoryOptionCombo()!!)
                } else {
                    val catOptionsInCategoryCombo = d2.categoryModule().categoryOptionCombos()
                        .withCategoryOptions()
                        .uid(it.categoryOptionCombo())
                        .blockingGet()
                        ?.categoryOptions()
                        ?.map { categoryOption -> categoryOption.uid() }
                        ?: emptyList()
                    val pivotedCategoryOptionUid =
                        pivotedCategoryOptionUids.find { uid -> uid in catOptionsInCategoryCombo }
                    val headerCategoryOptionsUids =
                        catOptionsInCategoryCombo.filter { uid -> uid != pivotedCategoryOptionUid }
                    Pair(
                        "${it.dataElement()!!}_$pivotedCategoryOptionUid",
                        headerCategoryOptionsUids.joinToString("_"),
                    )
                }
                key to it.value()
            }
    }

    override suspend fun value(
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ) = d2.dataValueModule().dataValues()
        .value(
            periodId,
            orgUnitUid,
            dataElementUid,
            categoryOptionComboUid,
            attrOptionComboUid,
        ).blockingGet()
        ?.value()

    override suspend fun updateValue(
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        value: String?,
    ): Result<Unit> {
        val valueRepository = d2.dataValueModule().dataValues()
            .value(
                period = periodId,
                organisationUnit = orgUnitUid,
                dataElement = dataElementUid,
                categoryOptionCombo = categoryOptionComboUid,
                attributeOptionCombo = attrOptionComboUid,
            )

        val dataElement = d2.dataElementModule().dataElements()
            .uid(dataElementUid).blockingGet()

        val validator = dataElement?.valueType()?.validator
        val fieldMask = dataElement?.fieldMask()

        return try {
            if (value.isNullOrEmpty()) {
                valueRepository.blockingDeleteIfExist()
            } else {
                val validValue = validator?.validate(value)?.getOrThrow()
                fieldMask?.let { mask ->
                    val fieldMaskValidation = FieldMaskValidator(mask).validate(value)
                    if (fieldMaskValidation is org.hisp.dhis.android.core.arch.helpers.Result.Failure) {
                        return Result.failure(fieldMaskValidation.failure)
                    }
                }
                valueRepository.blockingSet(validValue)
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun dataElementInfo(
        dataSetUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): DataElementInfo {
        val dataElement = d2.dataElementModule().dataElements()
            .uid(dataElementUid)
            .blockingGet()
        val categoryOptionCombo = d2.categoryModule().categoryOptionCombos()
            .uid(categoryOptionComboUid)
            .blockingGet()
        val isMandatory = d2.dataSetModule().dataSets().withCompulsoryDataElementOperands()
            .uid(dataSetUid)
            .blockingGet()
            ?.compulsoryDataElementOperands()
            ?.find {
                it.dataElement()?.uid() == dataElementUid &&
                    it.categoryOptionCombo()?.uid() == categoryOptionComboUid
            } != null
        val dataElementValueType = dataElement?.valueType()?.toInputType()
        val inputType = requireNotNull(dataElementValueType).takeIf {
            (it !is InputType.MultiText) && dataElement.optionSet()?.uid() == null
        }
            ?: if (dataElementValueType is InputType.MultiText) InputType.MultiText else InputType.OptionSet

        return DataElementInfo(
            label = "${dataElement.displayFormName()}/${categoryOptionCombo?.displayName()}",
            inputType = inputType,
            description = dataElement.displayDescription(),
            isRequired = isMandatory,
        )
    }

    override suspend fun getCoordinatesFrom(coordinatesValue: String): Pair<Double, Double> {
        val geometry = Geometry.builder()
            .coordinates(coordinatesValue)
            .type(FeatureType.POINT)
            .build()
        return GeometryHelper.getPoint(geometry).let {
            Pair(it[1], it[0])
        }
    }

    override suspend fun categoryOptionComboFromCategoryOptions(categoryOptions: List<String>): String {
        val categoryOptionCombos = d2.categoryModule().categoryOptionCombos()
            .byCategoryOptions(categoryOptions)
            .blockingGet()

        if (categoryOptionCombos.size != 1) throw IllegalStateException("More than one category option combo found")
        return categoryOptionCombos.first().uid()
    }

    private fun dataElementCategoryComboUid(dataElementUid: String?) =
        dataElementUid?.let {
            d2.dataElementModule().dataElements()
                .uid(dataElementUid)
                .blockingGet()
                ?.categoryComboUid()
        }

    override suspend fun getDataSetIndicator(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
        sectionUid: String,
    ) = d2.indicatorModule().indicators()
        .byDataSetUid(dataSetUid)
        .bySectionUid(sectionUid)
        .blockingGet()
        .associate { indicator ->
            (indicator.displayName() ?: indicator.uid()) to d2.indicatorModule()
                .dataSetIndicatorEngine()
                .blockingEvaluate(
                    indicator.uid(),
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attributeOptionComboUid,
                ).toString()
        }.toSortedMap(compareBy { it })
        .takeIf { it.isNotEmpty() }

    override suspend fun checkIfHasMissingMandatoryFields(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Boolean {
        return d2.dataSetModule().dataSetInstanceService()
            .blockingGetMissingMandatoryDataElementOperands(
                dataSetUid = dataSetUid,
                periodId = periodId,
                organisationUnitUid = orgUnitUid,
                attributeOptionComboUid = attributeOptionComboUid,
            ).isNotEmpty()
    }

    override suspend fun checkIfHasMissingMandatoryFieldsCombination(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Boolean {
        return d2.dataSetModule().dataSetInstanceService()
            .blockingGetMissingMandatoryFieldsCombination(
                dataSetUid = dataSetUid,
                periodId = periodId,
                organisationUnitUid = orgUnitUid,
                attributeOptionComboUid = attributeOptionComboUid,
            ).isNotEmpty()
    }

    override suspend fun completeDataset(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Result<Unit> {
        return try {
            d2.dataSetModule().dataSetCompleteRegistrations()
                .value(periodId, orgUnitUid, dataSetUid, attributeOptionComboUid)
                .blockingSet()
            Result.success(Unit)
        } catch (error: D2Error) {
            Result.failure(error)
        }
    }

    override suspend fun runValidationRules(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): ValidationRulesResult {
        val result = d2.validationModule()
            .validationEngine().validate(
                dataSetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
            ).blockingGet()

        return ValidationRulesResult(
            ValidationResultStatus.valueOf(result.status().name),
            mapViolations(
                violations = result.violations(),
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attrOptionComboUid = attrOptionComboUid,
            ),
        )
    }

    private fun mapViolations(
        violations: List<ValidationResultViolation>,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): List<Violation> {
        return violations.map {
            Violation(
                it.validationRule().description(),
                it.validationRule().instruction(),
                mapDataElements(
                    dataElementUids = it.dataElementUids(),
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            )
        }
    }

    private fun mapDataElements(
        dataElementUids: MutableSet<DataElementOperand>,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): List<DataToReview> {
        val dataToReview = arrayListOf<DataToReview>()
        dataElementUids.mapNotNull { deOperand ->
            d2.dataElementModule().dataElements()
                .uid(deOperand.dataElement()?.uid())
                .blockingGet()?.let {
                    Pair(deOperand, it)
                }
        }.forEach { (deOperand, de) ->
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
                        .value(
                            periodId,
                            orgUnitUid,
                            de.uid(),
                            catOptCombo.uid(),
                            attrOptionComboUid,
                        )
                        .blockingExists() &&
                    d2.dataValueModule().dataValues()
                        .value(
                            periodId,
                            orgUnitUid,
                            de.uid(),
                            catOptCombo.uid(),
                            attrOptionComboUid,
                        )
                        .blockingGet()?.deleted() != true
                ) {
                    d2.dataValueModule().dataValues()
                        .value(
                            periodId,
                            orgUnitUid,
                            de.uid(),
                            catOptCombo.uid(),
                            attrOptionComboUid,
                        )
                        .blockingGet()?.value() ?: "-"
                } else {
                    "-"
                }
                val isFromDefaultCatCombo = d2.categoryModule().categoryCombos()
                    .uid(catOptCombo.categoryCombo()?.uid()).blockingGet()?.isDefault == true
                dataToReview.add(
                    DataToReview(
                        de.uid(),
                        de.displayFormName(),
                        catOptCombo.uid(),
                        catOptCombo.displayName(),
                        value,
                        isFromDefaultCatCombo,
                    ),
                )
            }
        }
        return dataToReview
    }
}
