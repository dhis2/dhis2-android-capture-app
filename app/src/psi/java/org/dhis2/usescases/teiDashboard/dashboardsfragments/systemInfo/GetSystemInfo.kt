package org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo

interface SystemInfoRepository {
    fun get(): SystemInfo
}

class GetSystemInfo(private val systemInfoRepository: SystemInfoRepository) {
    operator fun invoke(): SystemInfo {
        return systemInfoRepository.get()
    }
}

