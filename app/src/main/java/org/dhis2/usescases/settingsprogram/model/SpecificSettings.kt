package org.dhis2.usescases.settingsprogram.model

import org.dhis2.mobile.commons.model.MetadataIconData

data class SpecificSettings(
    val name: String?,
    val description: String?,
    val metadataIconData: MetadataIconData,
)
