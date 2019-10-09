package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import io.reactivex.Flowable
import io.reactivex.Observable

internal interface HomeRepository {

    fun programModels(dateFilter: List<DatePeriod>, orgUnitFilter: List<String>, statesFilter: List<State>): Flowable<List<ProgramViewModel>>

    fun aggregatesModels(dateFilter: List<DatePeriod>, orgUnitFilter: List<String>, statesFilter: List<State>): Flowable<List<ProgramViewModel>>
}