package org.dhis2.usescases.reservedValue

import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress

class ReservedValueRepositoryImpl(
    private val d2: D2,
    private val prefs: PreferenceProvider,
    private val mapper: ReservedValueMapper,
) : ReservedValueRepository {
    override fun reservedValues(): Single<List<ReservedValueModel>> =
        d2.trackedEntityModule().reservedValueManager().getReservedValueSummaries()
            .map { mapper.map(it) }

    override fun refillReservedValues(uidToRefill: String): Observable<D2Progress> {
        val maxReservedValues = d2.settingModule().generalSetting()
            .blockingGet()
            ?.reservedValues() ?: prefs.getInt(Preference.NUMBER_RV, Preference.DEFAULT_NUMBER_RV)
        return d2.trackedEntityModule()
            .reservedValueManager()
            .downloadReservedValues(uidToRefill, maxReservedValues)
    }
}
