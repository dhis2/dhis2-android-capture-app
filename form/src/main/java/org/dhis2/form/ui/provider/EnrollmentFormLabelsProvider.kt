package org.dhis2.form.ui.provider

import kotlinx.coroutines.runBlocking
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.mobile.commons.providers.CustomLabelProvider

class EnrollmentFormLabelsProvider(
    val resourceManager: ResourceManager,
    private val customLabelProvider: CustomLabelProvider,
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

    fun provideCategoryComboSectionLabel(
        programUid: String,
        categoryComboName: String,
    ): String {
        val enrollmentLabel =
            runBlocking { customLabelProvider.getCustomEnrollmentLabel(programUid = programUid) }
        return resourceManager.getString(
            R.string.enrollment_category_combo_section_label,
            enrollmentLabel,
            categoryComboName,
        )
    }

    fun provideCategoryOptionNotValidForDate(optionName: String) =
        resourceManager.getString(R.string.category_option_not_valid_for_date, optionName)

    fun provideCategoryOptionNotInOrgUnit(optionName: String) =
        resourceManager.getString(R.string.category_option_not_in_org_unit, optionName)

    fun provideNoCategoryOptionsWithAccess(categoryName: String) =
        resourceManager.getString(R.string.no_category_options_with_access, categoryName)

    fun provideNoCategoryOptionsInOrgUnit(categoryName: String) =
        resourceManager.getString(R.string.no_category_options_in_org_unit, categoryName)

    fun provideNoCategoryOptionsForDate(categoryName: String) =
        resourceManager.getString(R.string.no_category_options_for_date, categoryName)
}
