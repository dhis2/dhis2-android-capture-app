package org.dhis2.usescases.reservedValue

import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.arch.call.D2Progress

interface ReservedValueRepository {
    fun reservedValues(): Single<List<ReservedValueModel>>
    fun refillReservedValues(uidToRefill: String): Observable<D2Progress>
}
