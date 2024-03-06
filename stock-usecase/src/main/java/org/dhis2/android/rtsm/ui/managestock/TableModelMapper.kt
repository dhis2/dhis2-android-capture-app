package org.dhis2.android.rtsm.ui.managestock

import javax.inject.Inject
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure

const val STOCK_TABLE_ID = "STOCK"
class TableModelMapper @Inject constructor(
    private val resources: ResourceManager
) {
    fun map(entries: List<StockEntry>, stockLabel: String, qtdLabel: String): List<TableModel> {
        val tableRowModels = mutableListOf<TableRowModel>()

        entries.forEachIndexed { index, entry ->
            val item = entry.item
            val tableRowModel = TableRowModel(
                rowHeader = RowHeader(
                    id = item.id,
                    title = item.name,
                    row = index
                ),
                values = mapOf(
                    Pair(
                        0,
                        TableCell(
                            id = "${item.id}_soh",
                            row = index,
                            column = 0,
                            editable = false,
                            value = entry.stockOnHand ?: item.stockOnHand
                        )
                    ),
                    Pair(
                        1,
                        TableCell(
                            id = "${item.id}_gty",
                            row = index,
                            column = 1,
                            value = entry.qty,
                            editable = true,
                            error = entry.errorMessage
                        )
                    )
                ),
                isLastRow = index == entries.lastIndex,
                maxLines = 3
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
                                TableHeaderCell(qtdLabel)
                            )
                        )
                    )
                ),
                tableRows = tableRowModels
            )
        )
    }

    fun validate(value: String?) = when (
        val result = ValueType.INTEGER_ZERO_OR_POSITIVE.validator.validate(value ?: "0")
    ) {
        is Result.Failure -> getIntegerZeroOrPositiveErrorMessage(
            result.failure as IntegerZeroOrPositiveFailure
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
