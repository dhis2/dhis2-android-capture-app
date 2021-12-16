package org.dhis2.form.model

data class RowAction(
    val id: String,
    val value: String? = null,
    val requiresExactMatch: Boolean = false,
    val optionCode: String? = null,
    val optionName: String? = null,
    val extraData: String? = null,
    val error: String? = null,
    val type: ActionType
)
