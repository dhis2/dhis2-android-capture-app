package org.dhis2.commons.periods.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

abstract class PeriodBaseRepository(
    private val d2: D2,

) : PeriodRepository {

    override fun generatePeriod(
        periodType: PeriodType,
        date: Date,
        offset: Int,
    ) = d2.periodModule().periodHelper()
        .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)
}
