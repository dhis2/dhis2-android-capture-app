package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class TableRowModel(
    val rowHeader: RowHeader,
    val values: Map<Int, TableCell>,
    val isLastRow: Boolean = false,
    val maxLines: Int = 3,
    val dropDownOptions: List<String>? = null
)
