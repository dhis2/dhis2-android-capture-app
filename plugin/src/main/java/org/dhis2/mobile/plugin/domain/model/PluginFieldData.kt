package org.dhis2.mobile.plugin.domain.model

/** The parts of a field's metadata a form field plugin receives. */
data class PluginFieldMetadata(
    val name: String,
    val shortName: String,
    val formName: String,
    val code: String?,
    val valueType: String,
    val description: String?,
    val optionSet: PluginOptionSet?,
)

data class PluginOptionSet(
    val code: String?,
    val name: String,
    val options: List<PluginOption>,
)

data class PluginOption(
    val uid: String,
    val code: String,
    val name: String,
)

/** A field's current state in the host form. */
data class PluginFieldState(
    val value: String?,
    val error: String?,
    val warning: String?,
    val editable: Boolean,
    val mandatory: Boolean,
)
