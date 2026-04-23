package org.dhis2.form.ui.plugin

data class SetFieldValueParams(
    val fieldId: String,
    val value: Any?,
    val options: FieldValueOptions? = null,
)

data class SetContextFieldValueParams(
    val fieldId: String,
    val value: Any?,
    val options: FieldValueOptions? = null,
)

data class FieldValueOptions(
    val valid: Boolean? = null,
    val touched: Boolean? = null,
    val error: String? = null,
)
