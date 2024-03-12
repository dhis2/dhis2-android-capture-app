package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.common.ObjectStyle
import java.util.Date

data class EventDetails(
    val name: String? = null,
    val description: String? = null,
    val style: ObjectStyle? = null,
    val enabled: Boolean = true,
    val isEditable: Boolean = true,
    val editableReason: String? = null,
    val temCreate: String? = null,
    val selectedDate: Date? = null,
    val selectedOrgUnit: String? = null,
    val catOptionComboUid: String? = null,
    val coordinates: String? = null,
    val isCompleted: Boolean = false,
    val isActionButtonVisible: Boolean = false,
    val actionButtonText: String? = null,
    val canReopen: Boolean = false,
)
