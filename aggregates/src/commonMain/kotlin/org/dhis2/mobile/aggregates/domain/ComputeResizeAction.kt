package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.model.ResizeSaveDimension
import org.dhis2.mobile.aggregates.ui.inputs.ResizeAction
import org.dhis2.mobile.commons.data.TableDimensionRepository

class ComputeResizeAction(
    private val dimensionRepository: TableDimensionRepository,
) {
    suspend operator fun invoke(resizeAction: ResizeAction): ResizeSaveDimension? =
        when (resizeAction) {
            is ResizeAction.ColumnHeaderChanged -> {
                dimensionRepository.saveColumnWidthForSection(
                    tableId = resizeAction.tableId,
                    sectionUid = resizeAction.sectionId,
                    column = resizeAction.columns,
                    widthDpValue = resizeAction.newValue,
                )
                null
            }

            is ResizeAction.Reset -> {
                dimensionRepository.resetTable(
                    tableId = resizeAction.tableId,
                    sectionUid = resizeAction.sectionId,
                )
                null
            }

            is ResizeAction.RowHeaderChanged -> {
                dimensionRepository.saveWidthForSection(
                    tableId = resizeAction.tableId,
                    sectionUid = resizeAction.sectionId,
                    widthDpValue = resizeAction.newValue,
                )
                null
            }

            is ResizeAction.TableDimension -> {
                dimensionRepository.saveTableWidth(
                    tableId = resizeAction.tableId,
                    sectionUid = resizeAction.sectionId,
                    widthDpValue = resizeAction.newValue,
                )
                null
            }

            is ResizeAction.GetTableSavedWidth -> {
                ResizeSaveDimension.Table(
                    dimensionRepository.getTableWidth(
                        sectionUid = resizeAction.sectionId,
                    ),
                )
            }

            is ResizeAction.GetColumSavedWidth -> {
                ResizeSaveDimension.ColumnHeader(
                    dimensionRepository.getColumnWidthForSection(
                        tableList = emptyList(),
                        sectionUid = resizeAction.sectionId,
                    ),
                )
            }

            is ResizeAction.GetRowHeaderSavedWidth ->
                ResizeSaveDimension.RowHeader(
                    dimensionRepository.getWidthForSection(
                        sectionUid = resizeAction.sectionId,
                    ),
                )
        }
}
