package org.dhis2.form.model

import org.hisp.dhis.mobile.ui.designsystem.component.SectionState

data class FormSection(
    val uid: String,
    val title: String,
    val description: String? = null,
    val state: SectionState,
    val fields: List<FieldUiModel>,
    var warningMessage: Int? = null,
    val completeFields: Int = 0,
    val totalFields: Int = 0,
    val warnings: Int = 0,
    val errors: Int = 0,
)
