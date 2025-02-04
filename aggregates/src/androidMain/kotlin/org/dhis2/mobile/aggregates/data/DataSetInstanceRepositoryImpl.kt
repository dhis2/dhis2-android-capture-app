package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.data.mappers.toDataSetDetails
import org.dhis2.mobile.aggregates.data.mappers.toDataSetSection
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataset.DataSetEditableStatus
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.datavalue.DataValueConflict
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

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

    override suspend fun getDataSetSectionData(
        dataSetUid: String,
        orgUnitUid: String,
        periodId: String,
        attrOptionComboUid: String,
        sectionUid: String,
    ): List<TableModel> {
        var absoluteRowIndex = 0
        val hasDataElementDecoration = dataSet(dataSetUid)?.dataElementDecoration() == true

        val (allDataSetElements, compulsoryDataElements) = d2.dataSetModule().dataSets()
            .withDataSetElements()
            .withCompulsoryDataElementOperands()
            .uid(dataSetUid)
            .blockingGet()?.let {
                Pair(
                    it.dataSetElements() ?: emptyList(),
                    it.compulsoryDataElementOperands() ?: emptyList(),
                )
            } ?: Pair(emptyList(), emptyList())

        val greyedOutFields = d2.dataSetModule().sections()
            .withGreyedFields()
            .withIndicators()
            .uid(sectionUid)
            .blockingGet()
            ?.greyedFields()

        val isDataSetEditable = d2.dataSetModule().dataSetInstanceService()
            .getEditableStatus(dataSetUid, periodId, orgUnitUid, attrOptionComboUid)
            .blockingGet() == DataSetEditableStatus.Editable

        return getSectionCategoryCombos(
            dataSetElements = allDataSetElements,
            dataSetUid = dataSetUid,
            sectionUid = sectionUid,
        ).entries.mapIndexed { catComboIndex, (categoryCombo, dataSetElements) ->

            val headerRows = categoryCombo.categories()?.map { category ->
                val catOptions = d2.categoryModule().categories()
                    .withCategoryOptions()
                    .uid(category.uid())
                    .blockingGet()
                    ?.categoryOptions()
                TableHeaderRow(
                    cells = catOptions?.map { categoryOption ->
                        TableHeaderCell(
                            value = categoryOption.displayName() ?: categoryOption.uid(),
                        )
                    } ?: emptyList(),
                )
            } ?: emptyList()

            val categoryOptionsCombos = categoryCombo.categories()?.mapNotNull { category ->
                val catOptions = d2.categoryModule().categories()
                    .withCategoryOptions()
                    .uid(category.uid())
                    .blockingGet()
                    ?.categoryOptions()
                catOptions?.mapNotNull { it.uid() }
            }?.fold(listOf(listOf<String>())) { acc, list ->
                acc.flatMap { existing ->
                    list.map { element ->
                        existing + element
                    }
                }
            }?.mapNotNull { categoryOptions ->
                d2.categoryModule().categoryOptionCombos()
                    .byCategoryOptions(categoryOptions)
                    .one()
                    .blockingGet()?.uid()
            } ?: emptyList()

            val tableHeader = TableHeader(
                rows = headerRows,
                hasTotals = d2.dataSetModule().sections()
                    .uid(sectionUid)
                    .blockingGet()
                    ?.showRowTotals() == true,
            )

            val tableRows = dataSetElements
                .mapIndexed { rowIndex, dataSetElement ->
                    val dataElement = d2.dataElementModule().dataElements()
                        .uid(dataSetElement.dataElement().uid())
                        .blockingGet()

                    TableRowModel(
                        rowHeader = RowHeader(
                            id = dataSetElement.dataElement().uid(),
                            title = dataElement?.displayFormName() ?: dataSetElement.dataElement()
                                .uid(),
                            row = absoluteRowIndex,
                            showDecoration = hasDataElementDecoration && dataElement?.displayDescription() != null,
                            description = dataElement?.displayDescription(),
                        ),
                        values = buildMap {
                            repeat(tableHeader.tableMaxColumns()) { columnIndex ->
                                if (dataElement != null) {
                                    val errorsAndWarnings = dataValueConflicts(
                                        dataSetUid = dataSetUid,
                                        periodId = periodId,
                                        orgUnitUid = orgUnitUid,
                                        attrOptionComboUid = attrOptionComboUid,
                                        dataElementUid = dataElement.uid(),
                                        categoryOptionComboUid = categoryOptionsCombos[columnIndex],
                                    )

                                    put(
                                        key = columnIndex,
                                        value = TableCell(
                                            id = dataElement.uid(),
                                            row = rowIndex,
                                            column = columnIndex,
                                            value = d2.dataValueModule().dataValues()
                                                .value(
                                                    period = periodId,
                                                    organisationUnit = orgUnitUid,
                                                    dataElement = dataElement.uid(),
                                                    categoryOptionCombo = categoryOptionsCombos[columnIndex],
                                                    attributeOptionCombo = attrOptionComboUid,
                                                )
                                                .blockingGet()
                                                ?.value(), // TODO: This should be parsed to a user friendly value
                                            editable = isDataSetEditable && greyedOutFields?.find {
                                                it.dataElement()?.uid() == dataElement.uid()
                                            } == null,
                                            mandatory = compulsoryDataElements.find {
                                                it.categoryOptionCombo()
                                                    ?.uid() == categoryOptionsCombos[columnIndex] &&
                                                    it.dataElement()?.uid() == dataElement.uid()
                                            } != null,
                                            error = errorsAndWarnings.first.joinToString(
                                                separator = ".\n",
                                            ) { it.conflict() ?: "" }.takeIf { it.isNotEmpty() },
                                            warning = errorsAndWarnings.second.joinToString(
                                                separator = ".\n",
                                            ) { it.conflict() ?: "" }.takeIf { it.isNotEmpty() },
                                            legendColor = null,
                                            isMultiText = dataElement.valueType() == ValueType.MULTI_TEXT,
                                        ),
                                    )
                                }
                            }
                        },
                        isLastRow = false, // TODO: This should not be needed
                        maxLines = 3,
                        dropDownOptions = null, /*TODO: This has to be requested on demand*/
                    ).also {
                        absoluteRowIndex += 1
                    }
                }

            TableModel(
                id = categoryCombo.uid(),
                title = categoryCombo.displayName() ?: "",
                tableHeaderModel = tableHeader,
                tableRows = tableRows,
                overwrittenValues = emptyMap(), /*TODO: This seems to not be used at all*/
            )
        }
    }

    private fun dataValueConflicts(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): Pair<List<DataValueConflict>, List<DataValueConflict>> {
        val conflicts = d2.dataValueModule().dataValueConflicts()
            .byDataSet(dataSetUid)
            .byPeriod().eq(periodId)
            .byOrganisationUnitUid().eq(orgUnitUid)
            .byAttributeOptionCombo().eq(attrOptionComboUid)
            .byDataElement().eq(dataElementUid)
            .byCategoryOptionCombo().eq(categoryOptionComboUid)
            .blockingGet()
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

    private fun dataSet(dataSetUid: String) = d2.dataSetModule().dataSets()
        .uid(dataSetUid)
        .blockingGet()

    private fun getSectionCategoryCombos(
        dataSetElements: List<DataSetElement>,
        dataSetUid: String,
        sectionUid: String,
    ): LinkedHashMap<CategoryCombo, List<DataSetElement>> {
        var dataSetElementsInSection = dataSetElements

        val catComboUids = when (sectionUid) {
            NO_SECTION_UID -> {
                dataSetElements.mapNotNull { dataSetElement ->
                    dataSetElement.categoryCombo()?.uid() ?: dataElementCategoryComboUid(
                        dataSetElement.dataElement().uid(),
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
                    it.dataElement().uid() in dataElementUidsInSection.orEmpty()
                }.sortedBy { dataElementUidsInSection?.indexOf(it.dataElement().uid()) }

                dataSetElementsInSection
                    .mapNotNull { dataSetElement ->
                        dataSetElement.categoryCombo()?.uid() ?: dataElementCategoryComboUid(
                            dataSetElement.dataElement().uid(),
                        )
                    }.distinct()
            }
        }

        return LinkedHashMap(
            d2.categoryModule().categoryCombos()
                .byUid().`in`(catComboUids)
                .withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet().associateWith { catCombo ->
                    dataSetElementsInSection.filter { dataSetElement ->
                        val catComboUid =
                            dataSetElement.categoryCombo()?.uid() ?: dataElementCategoryComboUid(
                                dataSetElement.dataElement().uid(),
                            )
                        catComboUid == catCombo.uid()
                    }
                },
        )
    }

    private fun dataElementCategoryComboUid(dataElementUid: String) =
        d2.dataElementModule().dataElements()
            .uid(dataElementUid)
            .blockingGet()
            ?.categoryComboUid()
}
