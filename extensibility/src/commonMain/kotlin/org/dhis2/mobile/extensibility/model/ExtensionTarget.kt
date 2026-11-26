package org.dhis2.mobile.extensibility.model

sealed interface ExtensionTarget {
    data class DataSet(
        val dataSetUid: String,
    ) : ExtensionTarget
}
