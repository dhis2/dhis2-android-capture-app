package org.dhis2.usescases.settings.models

import java.util.Date

data class ErrorViewModel(
    val creationDate: Date?,
    val creationDateLabel: String?,
    val errorCode: String?,
    val errorDescription: String?,
    val errorComponent: String?,
    val isSelected: Boolean = false,
)
