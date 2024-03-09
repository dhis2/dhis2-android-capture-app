package org.dhis2.form.model

import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

data class PeriodSelector(
    val type: PeriodType,
    val minDate: Date?,
    val maxDate: Date?,
)
