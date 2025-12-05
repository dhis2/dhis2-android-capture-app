package org.dhis2.form.ui.plugin

data class PluginProps(
    val uid: String,
    val label: String,
    val value: String?,
    val mandatory: Boolean,
    val readOnly: Boolean,
    val description: String?
)
