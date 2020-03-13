package org.dhis2.bindings

import org.dhis2.Bindings.EVERY_12_HOUR
import org.dhis2.Bindings.EVERY_24_HOUR
import org.dhis2.Bindings.EVERY_30_MIN
import org.dhis2.Bindings.EVERY_6_HOUR
import org.dhis2.Bindings.EVERY_7_DAYS
import org.dhis2.Bindings.EVERY_HOUR
import org.dhis2.Bindings.toSeconds
import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod
import org.junit.Test

class SettingsExtensionsTest {

    private val metadataSyncingPeriods = arrayListOf(
        EVERY_HOUR,
        EVERY_12_HOUR,
        EVERY_24_HOUR,
        EVERY_7_DAYS
    )

    private val dataSyncingPeriods = arrayListOf(
        EVERY_30_MIN,
        EVERY_HOUR,
        EVERY_6_HOUR,
        EVERY_12_HOUR,
        EVERY_24_HOUR,
        EVERY_7_DAYS
    )


    @Test
    fun `Metadata sync period must transform to proper integer`() {
        for (index in MetadataSyncPeriod.values().indices) {
            assert(MetadataSyncPeriod.values()[index].toSeconds() == metadataSyncingPeriods[index])
        }
    }

    @Test
    fun `Data sync period must transform to proper integer`() {
        for (index in DataSyncPeriod.values().indices) {
            assert(DataSyncPeriod.values()[index].toSeconds() == dataSyncingPeriods[index])
        }
    }
}