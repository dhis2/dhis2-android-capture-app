package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class TableHeaderRow(val cells: List<TableHeaderCell>)
