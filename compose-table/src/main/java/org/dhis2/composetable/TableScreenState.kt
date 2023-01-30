package org.dhis2.composetable

import java.util.UUID
import org.dhis2.composetable.model.TableModel

data class TableScreenState(
    val tables: List<TableModel>,
    val selectNext: Boolean,
    val id: UUID = UUID.randomUUID(),
    val overwrittenRowHeaderWidth: Float? = null,
    val textInputCollapsedMode: Boolean = true
)

data class TableConfigurationState(
    val overwrittenTableWidth: Map<String, Float>? = null,
    val overwrittenRowHeaderWidth: Map<String, Float>? = null,
    val overwrittenColumnWidth: Map<String, Map<Int, Float>>? = null
)
