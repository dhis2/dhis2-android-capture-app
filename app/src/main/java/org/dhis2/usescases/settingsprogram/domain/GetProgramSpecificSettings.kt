package org.dhis2.usescases.settingsprogram.domain

import org.dhis2.R
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settingsprogram.data.SettingsProgramRepository
import org.dhis2.usescases.settingsprogram.model.SpecificSettings
import org.hisp.dhis.android.core.settings.LimitScope

class GetProgramSpecificSettings(
    private val settingsProgramRepository: SettingsProgramRepository,
    private val resourceManager: ResourceManager,
    private val metadataIconProvider: MetadataIconProvider,
) {
    suspend operator fun invoke(): List<SpecificSettings> {
        val syncSettings = settingsProgramRepository.syncSettings()

        val programs =
            syncSettings?.programSettings()?.specificSettings()?.values?.map { programSetting ->
                val style = settingsProgramRepository.programStyle(programSetting.uid())
                SpecificSettings(
                    name = programSetting.name(),
                    description =
                        if (programSetting.eventsDownload() != null) {
                            "${programSetting.eventsDownload()} ${resourceManager.getString(R.string.events)} " +
                                trailingText(programSetting.settingDownload())
                        } else {
                            "${programSetting.teiDownload()} ${resourceManager.getString(R.string.teis)} " +
                                trailingText(programSetting.settingDownload())
                        },
                    metadataIconData = metadataIconProvider(style),
                )
            } ?: emptyList()

        val dataSets =
            syncSettings?.dataSetSettings()?.specificSettings()?.values?.map { dataSetSetting ->
                val style = settingsProgramRepository.dataSetStyle(dataSetSetting.uid())
                SpecificSettings(
                    name = dataSetSetting.name(),
                    description =
                        "${dataSetSetting.periodDSDownload()} " +
                            resourceManager.getString(R.string.period),
                    metadataIconData = metadataIconProvider(style),
                )
            } ?: emptyList()

        return (programs + dataSets).sortedBy { it.name?.lowercase() }
    }

    private fun trailingText(limitScope: LimitScope?): String =
        when (limitScope) {
            LimitScope.ALL_ORG_UNITS -> resourceManager.getString(R.string.limit_scope_all_ou_trailing)
            LimitScope.PER_ORG_UNIT -> resourceManager.getString(R.string.limit_scope_ou_trailing)
            LimitScope.PER_PROGRAM -> resourceManager.getString(R.string.limit_scope_program_trailing)
            LimitScope.PER_OU_AND_PROGRAM -> resourceManager.getString(R.string.limit_scope_ou_program_trailing)
            else -> resourceManager.getString(R.string.limit_scope_global_trailing)
        }
}
