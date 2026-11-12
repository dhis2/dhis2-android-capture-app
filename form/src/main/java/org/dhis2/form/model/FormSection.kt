package org.dhis2.form.model

import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

data class FormSection(
    val uid: String,
    val title: String,
    val description: String? = null,
    val state: SectionState,
    val fields: List<FieldUiModel>,
    val warningMessage: Int? = null,
    val completeFields: Int = 0,
    val totalFields: Int = 0,
    val warnings: Int = 0,
    val errors: Int = 0,
    /** Form field plugins drawn among [fields], from the form's configured layout. */
    val plugins: List<SectionPlugin> = emptyList(),
)

/**
 * A plugin drawn in a section, before the field at [position] in its fields, or after the last one
 * when [position] is the number of fields.
 */
data class SectionPlugin(
    val elementId: String,
    val position: Int,
)
