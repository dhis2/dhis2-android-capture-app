package org.dhis2.commons.periods.data

import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

interface PeriodRepository {

    fun generatePeriod(
        periodType: PeriodType,
        date: Date = Date(),
        offset: Int = 0,
    ): Period
}
