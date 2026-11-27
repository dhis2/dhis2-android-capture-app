package org.dhis2.form.ui.provider

import kotlinx.coroutines.runBlocking
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.mobile.commons.category.domain.model.CategorySelectionError
import org.dhis2.mobile.commons.category.domain.model.NoOptionsReason
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

    fun provideCategorySelectionError(
        error: CategorySelectionError,
        optionName: String,
    ) = resourceManager.getString(
        when (error) {
            CategorySelectionError.OUT_OF_DATE_RANGE -> R.string.category_option_not_valid_for_date
            CategorySelectionError.NOT_IN_ORG_UNIT -> R.string.category_option_not_in_org_unit
        },
        optionName,
    )

    fun provideNoCategoryOptionsReason(
        reason: NoOptionsReason,
        categoryName: String,
    ) = resourceManager.getString(
        when (reason) {
            NoOptionsReason.NO_ACCESS -> R.string.no_category_options_with_access
            NoOptionsReason.NOT_IN_ORG_UNIT -> R.string.no_category_options_in_org_unit
            NoOptionsReason.NOT_VALID_FOR_DATE -> R.string.no_category_options_for_date
        },
        categoryName,
    )
}
