package org.dhis2.composetable.model.extensions

import org.dhis2.composetable.model.TableModel

fun TableModel.areAllValuesEmpty(): Boolean {
    this.tableRows.forEach { row ->
        val result = row.values.values.filterNot { it.value == "" }
        if (result.isNotEmpty()) {
            return false
        }
    }
    return true
}
