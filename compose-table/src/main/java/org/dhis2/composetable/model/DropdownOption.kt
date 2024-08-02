package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class DropdownOption(
    val code: String,
    val name: String,
)
