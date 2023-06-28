package org.dhis2.form.ui.provider

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R

class EnrollmentFormLabelsProvider(val resourceManager: ResourceManager) {

    fun provideEnrollmentDataSectionLabel() =
        resourceManager.getString(R.string.enrollment_data_section_label)

    fun provideSingleSectionLabel() =
        resourceManager.getString(R.string.enrollment_single_section_label)

    fun provideEnrollmentOrgUnitLabel() = resourceManager.getString(R.string.enrolling_ou)

    fun provideTeiCoordinatesLabel() = resourceManager.getString(R.string.tei_coordinates)

    fun provideEnrollmentCoordinatesLabel() =
        resourceManager.getString(R.string.enrollment_coordinates)

    fun provideReservedValueWarning() = resourceManager.getString(R.string.no_reserved_values)

    fun provideEnrollmentDateDefaultLabel() = resourceManager.getString(R.string.enrollmment_date)

    fun provideIncidentDateDefaultLabel() = resourceManager.getString(R.string.incident_date)
}
