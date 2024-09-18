package org.dhis2.usescases.programStageSelection

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.program.ProgramStage

data class ProgramStageData(
    val programStage: ProgramStage,
    val metadataIconData: MetadataIconData,
)
