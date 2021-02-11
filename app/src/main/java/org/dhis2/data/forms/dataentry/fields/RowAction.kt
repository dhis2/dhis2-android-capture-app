package org.dhis2.data.forms.dataentry.fields

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

enum class ActionType {
    ON_NEXT, ON_FOCUS, ON_SAVE, ON_TEXT_CHANGE
}
