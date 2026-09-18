package org.dhis2.usescases.settings.models

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class ErrorViewModel(
    val creationDate: Date?,
    val creationDateLabel: String?,
    val errorCode: String?,
    val errorDescription: String?,
    val errorComponent: String?,
    var isSelected: Boolean = false,
)
