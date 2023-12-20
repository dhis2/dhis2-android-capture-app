package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class TableCell(
    val id: String? = null,
    val row: Int? = null,
    val column: Int,
    val value: String?,
    val editable: Boolean = true,
    val mandatory: Boolean? = false,
    val error: String? = null,
    val warning: String? = null,
    val legendColor: Int? = null,
) {

    fun hasErrorOrWarning() = errorOrWarningMessage() != null
    fun errorOrWarningMessage() = error ?: warning
}
