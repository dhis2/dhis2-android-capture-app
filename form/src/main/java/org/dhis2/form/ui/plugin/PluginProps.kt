package org.dhis2.form.ui.plugin

data class PluginProps(
    val values: Map<String, Any?>,
    val errors: Map<String, List<String>>,
    val warnings: Map<String, List<String>>,
    val formSubmitted: Boolean,
    val fieldsMetadata: Map<String, FieldMetadata>,
)

data class FieldMetadata(
    val id: String,
    val name: String,
    val shortName: String,
    val formName: String,
    val disabled: Boolean,
    val compulsory: Boolean,
    val description: String?,
    val type: String,
    val optionSet: Any?,
    val displayInForms: Boolean,
    val displayInReports: Boolean,
    val icon: Any?,
    val unique: Any?,
    val searchable: Boolean?,
    val url: String?,
)
