package org.dhis2.composetable.data

import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

const val input_error_message = "This has an error"

val tableData = listOf(
    TableModel(
        id = "table",
        tableHeaderModel = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableHeaderCell("A"),
                        TableHeaderCell("B"),
                    )
                ),
                TableHeaderRow(
                    cells = listOf(
                        TableHeaderCell("1"),
                        TableHeaderCell("2"),
                        TableHeaderCell("3")
                    )
                )
            )
        ),
        tableRows = listOf(
            TableRowModel(
                rowHeader = RowHeader(
                    id = "0",
                    title = "Row 1",
                    row = 0
                ),
                values = mapOf(
                    Pair(0, TableCell("00", 0, 0, "12")),
                    Pair(1, TableCell("01", 0, 1, value = "-1", error = input_error_message))
                ),
            ),
            TableRowModel(
                rowHeader = RowHeader(
                    id = "1",
                    title = "Row 2",
                    row = 1
                ),
                values = emptyMap()
            ),
        ),
    )
)