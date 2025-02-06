package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.data.mappers.toDataSetDetails
import org.dhis2.mobile.aggregates.data.mappers.toDataSetSection
import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.MandatoryCellElements
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataset.DataSetEditableStatus
import org.hisp.dhis.android.core.dataset.Section

internal class DataSetInstanceRepositoryImpl(
    private val d2: D2,
) : DataSetInstanceRepository {

    override fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ) = d2.dataSetModule().dataSetInstances()
        .byDataSetUid().eq(dataSetUid)
        .byPeriod().eq(periodId)
        .byOrganisationUnitUid().eq(orgUnitUid)
        .byAttributeOptionComboUid().eq(attrOptionComboUid)
        .blockingGet()
        .map { dataSetInstance ->
            val catComboUid = d2.dataSetModule().dataSets()
                .uid(dataSetUid)
                .blockingGet()
                ?.categoryCombo()?.uid()
            val isDefaultCatCombo = d2.categoryModule().categoryCombos()
                .uid(catComboUid)
                .blockingGet()
                ?.isDefault
            dataSetInstance.toDataSetDetails(isDefaultCatCombo = isDefaultCatCombo == true)
        }
        .first()

    override fun getDataSetInstanceSections(
        dataSetUid: String,
    ) = d2.dataSetModule().sections()
        .byDataSetUid().eq(dataSetUid)
        .blockingGet().map(Section::toDataSetSection)

    override fun getRenderingConfig(
        dataSetUid: String,
    ) = d2.dataSetModule().dataSets()
        .uid(dataSetUid)
        .blockingGet()?.let {
            DataSetRenderingConfig(
                useVerticalTabs = it.renderHorizontally() != true,
            )
        } ?: DataSetRenderingConfig(
        useVerticalTabs = true,
    )

    override fun categoryOptionCombinations(categoryUids: List<String>): List<String> {
        return categoryUids.mapNotNull { categoryUid ->
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
            d2.categoryModule().categoryOptionCombos()
                .byCategoryOptions(categoryOptions)
                .one()
                .blockingGet()?.uid()
        }
    }

    override fun conflicts(
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
            .byDataSetUid(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionComboUid().eq(attrOptionComboUid)
            .byDataElementUid().eq(dataElementUid)
            .byCategoryOptionComboUid().eq(categoryOptionComboUid)
            .blockingGet()
            .firstOrNull()
            ?.syncState()

        return when (syncState) {
            State.ERROR -> Pair(conflicts, emptyList())
            State.WARNING -> Pair(emptyList(), conflicts)
            else -> Pair(emptyList(), emptyList())
        }
    }

    override fun dataSetInstanceConfiguration(
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

    override fun dataSetInstanceSectionConfiguration(sectionUid: String) =
        d2.dataSetModule().sections()
            .uid(sectionUid)
            .blockingGet()?.let { section ->
                DataSetInstanceSectionConfiguration(
                    showRowTotals = section.showRowTotals() == true,
                )
            }

    override fun getDataSetInstanceSectionCells(
        dataSetElements: List<CellElement>,
        dataSetUid: String,
        sectionUid: String,
    ): List<DataSetInstanceSectionData> {
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

        return d2.categoryModule().categoryCombos()
            .byUid().`in`(catComboUids)
            .withCategories()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .blockingGet().map { catCombo ->
                DataSetInstanceSectionData(
                    uid = catCombo.uid(),
                    label = catCombo.displayName() ?: "",
                    subgroups = catCombo.categories()?.mapNotNull { it.uid() } ?: emptyList(),
                    cellElements = dataSetElementsInSection.filter { dataSetElement ->
                        val catComboUid =
                            dataSetElement.categoryComboUid ?: dataElementCategoryComboUid(
                                dataSetElement.uid,
                            )
                        catComboUid == catCombo.uid()
                    },
                )
            }
    }

    override fun getTableGroupHeaders(categoryUids: List<String>): List<List<String>> {
        return categoryUids.mapNotNull { categoryUid ->
            d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(categoryUid)
                .blockingGet()
                ?.categoryOptions()?.map { categoryOption ->
                    categoryOption.displayName() ?: categoryOption.uid()
                }
        }
    }

    override fun cellValue(
        periodId: String,
        orgUnitUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        attrOptionComboUid: String,
    ) = d2.dataValueModule().dataValues()
        .value(
            period = periodId,
            organisationUnit = orgUnitUid,
            dataElement = dataElementUid,
            categoryOptionCombo = categoryOptionComboUid,
            attributeOptionCombo = attrOptionComboUid,
        )
        .blockingGet()
        ?.value()

    private fun dataElementCategoryComboUid(dataElementUid: String?) =
        dataElementUid?.let {
            d2.dataElementModule().dataElements()
                .uid(dataElementUid)
                .blockingGet()
                ?.categoryComboUid()
        }
}
