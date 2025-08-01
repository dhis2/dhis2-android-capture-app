package org.dhis2.usescases.teiDashboard.teiProgramList

import org.dhis2.mobile.commons.model.MetadataIconData

data class EnrollmentViewModel(
    val uid: String,
    val enrollmentDate: String,
    val metadataIconData: MetadataIconData?,
    val programName: String,
    val orgUnitName: String,
    val followUp: Boolean,
    val programUid: String,
)
