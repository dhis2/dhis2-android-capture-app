package org.dhis2.form.model

import org.hisp.dhis.mobile.ui.designsystem.component.LegendDescriptionData

data class LegendValue(
    val color: Int,
    val label: String?,
    val legendsInfo: List<LegendDescriptionData>?,
)
