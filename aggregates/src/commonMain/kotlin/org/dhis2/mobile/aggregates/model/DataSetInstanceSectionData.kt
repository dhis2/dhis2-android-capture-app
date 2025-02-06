package org.dhis2.mobile.aggregates.model

data class DataSetInstanceSectionData(
    val uid: String,
    val label: String,
    val subgroups: List<String>,
    val cellElements: List<CellElement>,
)
