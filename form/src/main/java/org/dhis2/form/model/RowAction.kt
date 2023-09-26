package org.dhis2.form.model

import org.hisp.dhis.android.core.common.ValueType

data class RowAction(
    val id: String,
    val value: String? = null,
    val requiresExactMatch: Boolean = false,
    val optionCode: String? = null,
    val optionName: String? = null,
    val extraData: String? = null,
    val error: Throwable? = null,
    val type: ActionType,
    val valueType: ValueType? = null,
)
