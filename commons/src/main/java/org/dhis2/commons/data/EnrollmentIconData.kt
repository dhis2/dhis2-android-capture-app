package org.dhis2.commons.data

import org.dhis2.ui.MetadataIconData

data class EnrollmentIconData(
    val color: Int,
    val imageResource: Int,
    val isIcon: Boolean,
    val remainingEnrollments: Int,
    val metadataIconData: MetadataIconData,
)
