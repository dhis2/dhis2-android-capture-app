package org.dhis2.composetable

import java.util.UUID
import org.dhis2.composetable.model.TableModel

data class TableScreenState(
    val tables: List<TableModel>,
    val selectNext: Boolean,
    val id: UUID = UUID.randomUUID(),
    val overwrittenRowHeaderWidth: Float? = null
)
