package org.dhis2.mobile.extensibility.model

data class Extension(
    val id: String,
    val type: ExtensionType,
    val target: ExtensionTarget,
)

enum class ExtensionType { CUSTOM_FORM }
