package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.category.CategoryOption
import java.util.Date

data class EventCatComboUiModel(
    val category: EventCategory,
    val eventCatCombo: EventCatCombo,
    val detailsEnabled: Boolean,
    val currentDate: Date?,
    val selectedOrgUnit: String?,
    val onShowCategoryDialog: (EventCategory) -> Unit,
    val onClearCatCombo: (EventCategory) -> Unit,
    val onOptionSelected: (CategoryOption?) -> Unit,
    val required: Boolean = false,
    val noOptionsText: String,
    val catComboText: String,
    val showField: Boolean = true,
)
