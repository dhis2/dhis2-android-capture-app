package org.dhis2.mobile.extensibility.host.model

/**
 * The data set instance a custom form may read and write. The renderer checks every write
 * against it in Kotlin (trust boundary), never only in JavaScript.
 */
data class DataSetInstance(
    val dataSetUid: String,
    val periodId: String,
    val organisationUnitUid: String,
    val attributeOptionComboUid: String,
)
