package org.dhis2.composetable

import java.util.UUID
import org.dhis2.composetable.model.TableModel

data class TableScreenState(
    val tables: List<TableModel>,
    val id: UUID = UUID.randomUUID()
)

data class TableConfigurationState(
    val overwrittenTableWidth: Map<String, Float>? = null,
    val overwrittenRowHeaderWidth: Map<String, Float>? = null,
    val overwrittenColumnWidth: Map<String, Map<Int, Float>>? = null
) {
    fun isResized() = !overwrittenTableWidth.isNullOrEmpty() or
        !overwrittenRowHeaderWidth.isNullOrEmpty() or
        !overwrittenColumnWidth.isNullOrEmpty()
}
