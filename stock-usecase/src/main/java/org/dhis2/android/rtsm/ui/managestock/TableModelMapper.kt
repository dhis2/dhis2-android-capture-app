package org.dhis2.android.rtsm.ui.managestock

import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.commons.resources.ResourceManager

import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.composetable.model.TableRowModel
import javax.inject.Inject

const val STOCK_TABLE_ID = "STOCK"
class TableModelMapper @Inject constructor(
    private val resources: ResourceManager,
) {
    fun map(entries: List<StockEntry>, stockLabel: String, qtdLabel: String): List<TableModel> {
        val tableRowModels = mutableListOf<TableRowModel>()

        entries.forEachIndexed { index, entry ->
            val item = entry.item
            val tableRowModel = TableRowModel(
                rowHeader = RowHeader(
                    id = item.id,
                    title = item.name,
                    row = index,
                ),
                values = mapOf(
                    Pair(
                        0,
                        TableCell(
                            id = "${item.id}_soh",
                            row = index,
                            column = 0,
                            editable = false,
                            value = entry.stockOnHand ?: item.stockOnHand,
                        ),
                    ),
                    Pair(
                        1,
                        TableCell(
                            id = "${item.id}_gty",
                            row = index,
                            column = 1,
                            value = entry.qty,
                            editable = true,
                            error = entry.errorMessage,
                        ),
                    ),
                ),
                isLastRow = index == entries.lastIndex,
                maxLines = 3,
            )

            tableRowModels.add(tableRowModel)
        }

        return mutableListOf(
            TableModel(
                id = STOCK_TABLE_ID,
                tableHeaderModel = TableHeader(
                    rows = mutableListOf(
                        TableHeaderRow(
                            mutableListOf(
                                TableHeaderCell(stockLabel),
                                TableHeaderCell(qtdLabel),
                            ),
                        ),
                    ),
                ),
                tableRows = tableRowModels,
            ),
        )
    }

    fun validate(value: String?) = when (
        val result = ValueType.INTEGER_ZERO_OR_POSITIVE.validator.validate(value ?: "0")
    ) {
        is Result.Failure -> getIntegerZeroOrPositiveErrorMessage(
            result.failure as IntegerZeroOrPositiveFailure,
        )
        else -> null
    }

    private fun getIntegerZeroOrPositiveErrorMessage(error: IntegerZeroOrPositiveFailure) =
        when (error) {
            IntegerZeroOrPositiveFailure.IntegerOverflow ->
                resources.getString(R.string.formatting_error)
            IntegerZeroOrPositiveFailure.NumberFormatException ->
                resources.getString(R.string.formatting_error)
            IntegerZeroOrPositiveFailure.ValueIsNegative ->
                resources.getString(R.string.invalid_possitive_zero)
            IntegerZeroOrPositiveFailure.LeadingZeroException ->
                resources.getString(R.string.leading_zero_error)
        }
}
