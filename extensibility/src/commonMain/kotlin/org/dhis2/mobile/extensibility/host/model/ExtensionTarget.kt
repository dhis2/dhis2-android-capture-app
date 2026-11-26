package org.dhis2.mobile.extensibility.host.model

sealed interface ExtensionTarget {
    data class DataSet(
        val dataSetUid: String,
    ) : ExtensionTarget
}
