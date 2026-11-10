package org.dhis2.mobile.extension.host.model

/**
 * Anything from the server that the app runs in a fixed place: today a data set custom form;
 * later a web plugin or a native module.
 *
 * Where its content comes from (SDK `htmlCode`, a bundle path) is not part of the model: it stays
 * behind a repository, so commonMain never sees file paths or SDK types.
 */
data class Extension(
    val id: String,
    val kind: ExtensionKind,
    val target: ExtensionTarget,
) {
    companion object {
        fun customForm(dataSetUid: String) =
            Extension(
                id = "customForm:$dataSetUid",
                kind = ExtensionKind.CUSTOM_FORM,
                target = ExtensionTarget.DataSet(dataSetUid),
            )
    }
}

/** Values are added when their content type ships: WEB_PLUGIN in Next, NATIVE_MODULE later. */
enum class ExtensionKind { CUSTOM_FORM, }

/** Where an extension applies. FormSection(formUid, sectionId) arrives with form field plugins. */
sealed interface ExtensionTarget {
    data class DataSet(
        val dataSetUid: String,
    ) : ExtensionTarget
}
