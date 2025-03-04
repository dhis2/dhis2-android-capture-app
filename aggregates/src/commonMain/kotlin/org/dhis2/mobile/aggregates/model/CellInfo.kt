package org.dhis2.mobile.aggregates.model

import org.dhis2.mobile.aggregates.ui.states.InputExtra
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData

internal data class CellInfo(
    val label: String,
    val value: String?,
    val inputType: InputType,
    val inputExtra: InputExtra,
    val supportingText: List<String>,
    val errors: List<String>,
    val warnings: List<String>,
    val legendData: LegendData?,
    val isRequired: Boolean,
)
