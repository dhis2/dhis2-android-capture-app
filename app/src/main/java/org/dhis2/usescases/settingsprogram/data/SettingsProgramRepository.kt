package org.dhis2.usescases.settingsprogram.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle

class SettingsProgramRepository(
    private val d2: D2,
) {
    suspend fun syncSettings() =
        d2
            .settingModule()
            .synchronizationSettings()
            .blockingGet()

    suspend fun programStyle(programUid: String?): ObjectStyle =
        d2
            .programModule()
            .programs()
            .uid(programUid)
            .blockingGet()
            ?.style() ?: ObjectStyle.builder().build()

    suspend fun dataSetStyle(dataSetUid: String?): ObjectStyle =
        d2
            .dataSetModule()
            .dataSets()
            .uid(dataSetUid)
            .blockingGet()
            ?.style() ?: ObjectStyle.builder().build()
}
