package org.dhis2.mobile.extension.host.model

/**
 * The one data set instance a custom form may read and write. The renderer checks every write
 * against it in Kotlin (trust boundary), never only in JavaScript.
 */
data class DataSetInstanceRef(
    val dataSetUid: String,
    val periodId: String,
    val organisationUnitUid: String,
    val attributeOptionComboUid: String,
)
