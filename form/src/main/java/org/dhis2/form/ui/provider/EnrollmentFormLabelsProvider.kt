package org.dhis2.form.ui.provider

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R

class EnrollmentFormLabelsProvider(
    val resourceManager: ResourceManager,
) {
    fun provideEnrollmentDataSectionLabel(programUid: String) =
        resourceManager.formatWithEnrollmentLabel(
            programUid,
            R.string.enrollment_data_section_label_V2,
            1,
        )

    fun provideSingleSectionLabel() = resourceManager.getString(R.string.enrollment_single_section_label)

    fun provideEnrollmentOrgUnitLabel() = resourceManager.getString(R.string.enrolling_ou)

    fun provideTeiCoordinatesLabel() = resourceManager.getString(R.string.tei_coordinates)

    fun provideEnrollmentCoordinatesLabel(programUid: String) =
        resourceManager.formatWithEnrollmentLabel(
            programUid,
            R.string.enrollment_coordinates_V2,
            1,
        )

    fun provideReservedValueWarning() = resourceManager.getString(R.string.no_reserved_values)

    fun provideEnrollmentDateDefaultLabel(programUid: String) =
        resourceManager.formatWithEnrollmentLabel(
            programUid,
            R.string.enrollment_date_V2,
            1,
        )

    fun provideIncidentDateDefaultLabel() = resourceManager.getString(R.string.incident_date)
}
