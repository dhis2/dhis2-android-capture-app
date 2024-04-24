package org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo

import org.hisp.dhis.android.core.D2

class SystemInfoD2Repository(private val d2: D2) : SystemInfoRepository {
    override fun get(): SystemInfo {
        val d2SystemInfo = d2.systemInfoModule().systemInfo().blockingGet();

        return SystemInfo(
            d2SystemInfo!!.serverDate()!!,
            d2SystemInfo.dateFormat()!!,
            d2SystemInfo.version()!!,
            d2SystemInfo.contextPath()!!,
            d2SystemInfo.systemName()!!
        )
    }
}