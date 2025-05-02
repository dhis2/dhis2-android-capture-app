package org.dhis2.mobile.aggregates.model

data class DataSetInstanceParameters(
    val dataSetUid: String,
    val periodId: String,
    val organisationUnitUid: String,
    val attributeOptionComboUid: String,
    val openErrorLocation: Boolean,
)
