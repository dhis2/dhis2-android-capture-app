package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.period.DatePeriod

internal interface HomeRepository {

    fun programModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>
    ): Flowable<List<ProgramViewModel>>

    fun aggregatesModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>
    ): Flowable<List<ProgramViewModel>>
}
