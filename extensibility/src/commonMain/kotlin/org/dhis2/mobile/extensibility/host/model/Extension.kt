package org.dhis2.mobile.extensibility.host.model

// TODO add documentation
// TODO is it worth converting this into a sealed class
sealed class Extension(
    val id: String,
    val type: ExtensionType,
    val target: ExtensionTarget,
) {
    data class CustomForm(
        val dataSetUid: String,
    ) : Extension(
            // TODO consider whether the "customForm:" string is needed
            id = "customForm:$dataSetUid",
            type = ExtensionType.CUSTOM_FORM,
            target = ExtensionTarget.DataSet(dataSetUid),
        )
}

enum class ExtensionType { CUSTOM_FORM }
