package org.dhis2.mobile.extensibility.host.model

sealed class Extension(
    val id: String,
    val type: ExtensionType,
    val target: ExtensionTarget,
) {
    data class CustomForm(
        val dataSetUid: String,
    ) : Extension(
            id = dataSetUid,
            type = ExtensionType.CUSTOM_FORM,
            target = ExtensionTarget.DataSet(dataSetUid),
        )
}

enum class ExtensionType { CUSTOM_FORM }
