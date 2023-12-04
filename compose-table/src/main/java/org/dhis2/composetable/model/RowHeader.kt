package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class RowHeader(
    val id: String? = null,
    val title: String,
    val row: Int,
    val showDecoration: Boolean = false,
    val description: String? = null,
)
