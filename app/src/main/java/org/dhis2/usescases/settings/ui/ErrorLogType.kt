package org.dhis2.usescases.settings.ui

import androidx.compose.runtime.Stable

@Stable
sealed interface ErrorLogType {
    object MetadataIntegrity : ErrorLogType

    object Sync : ErrorLogType
}
