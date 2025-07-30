package org.dhis2.usescases.settingsprogram.model

import org.dhis2.ui.MetadataIconData

data class SpecificSettings(
    val name: String?,
    val description: String?,
    val metadataIconData: MetadataIconData,
)
