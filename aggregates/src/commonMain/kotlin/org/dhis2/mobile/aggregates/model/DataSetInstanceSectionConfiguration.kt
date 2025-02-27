package org.dhis2.mobile.aggregates.model

internal data class DataSetInstanceSectionConfiguration(
    val showRowTotals: Boolean,
    val showColumnTotals: Boolean,
    val pivotedHeaderId: String?,
)
